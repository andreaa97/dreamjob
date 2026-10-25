package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.ApplicationStatus;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.ApplicationNotFoundException;
import com.andrea.remoteflow.exception.DuplicateApplicationException;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test della logica di business di {@link ApplicationService}, senza Spring:
 * i repository sono mock di Mockito e {@code @InjectMocks} li passa al service.
 * Si verificano le regole (job inesistente, candidatura duplicata...) e le chiamate ai repository.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    // ------------------------------------------------------------
    // Helper: i setter di id non esistono nelle entita', quindi
    // usiamo la reflection solo nei test.
    // ------------------------------------------------------------

    private Job job(Long id) {
        Job job = new Job("Backend Developer", "Acme", "Remote", 35000L, 50000L, true);
        ReflectionTestUtils.setField(job, "id", id);
        return job;
    }

    private Application application(Long id, Job job, ApplicationStatus status) {
        Application application = new Application();
        ReflectionTestUtils.setField(application, "id", id);
        application.setJob(job);
        application.setStatus(status);
        application.setAppliedAt(LocalDate.of(2026, 10, 1));
        return application;
    }

    private ApplicationRequest request(Long jobId, ApplicationStatus status, LocalDate appliedAt) {
        return new ApplicationRequest(jobId, status, appliedAt);
    }

    // ============================================================
    // createApplication
    // ============================================================

    @Nested
    @DisplayName("createApplication")
    class CreateApplication {

        @Test
        @DisplayName("salva e restituisce la candidatura quando il job esiste e non ci sono duplicati")
        void createsApplication() {
            // given
            Job job = job(1L);
            ApplicationRequest request = request(1L, ApplicationStatus.APPLIED, LocalDate.of(2026, 10, 7));

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(applicationRepository.existsByJobId(1L)).thenReturn(false);
            when(applicationRepository.save(any(Application.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Application result = applicationService.createApplication(request);

            // then
            assertSame(job, result.getJob());
            assertEquals(ApplicationStatus.APPLIED, result.getStatus());
            assertEquals(LocalDate.of(2026, 10, 7), result.getAppliedAt());

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertSame(job, captor.getValue().getJob());
        }

        @Test
        @DisplayName("lancia JobNotFoundException se il job non esiste")
        void throwsWhenJobNotFound() {
            ApplicationRequest request = request(99L, ApplicationStatus.APPLIED, LocalDate.now());
            when(jobRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(JobNotFoundException.class,
                    () -> applicationService.createApplication(request));

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("lancia DuplicateApplicationException se esiste gia' una candidatura per il job")
        void throwsWhenDuplicate() {
            Job job = job(1L);
            ApplicationRequest request = request(1L, ApplicationStatus.APPLIED, LocalDate.now());

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(applicationRepository.existsByJobId(1L)).thenReturn(true);

            assertThrows(DuplicateApplicationException.class,
                    () -> applicationService.createApplication(request));

            verify(applicationRepository, never()).save(any());
        }
    }

    // ============================================================
    // getAllApplications / getApplicationById
    // ============================================================

    @Nested
    @DisplayName("lettura")
    class Reading {

        @Test
        @DisplayName("getAllApplications restituisce tutte le candidature")
        void returnsAll() {
            List<Application> applications = List.of(
                    application(1L, job(1L), ApplicationStatus.APPLIED),
                    application(2L, job(2L), ApplicationStatus.INTERVIEW)
            );
            when(applicationRepository.findAll()).thenReturn(applications);

            List<Application> result = applicationService.getAllApplications();

            assertEquals(2, result.size());
            assertEquals(applications, result);
        }

        @Test
        @DisplayName("getApplicationById restituisce la candidatura se esiste")
        void returnsById() {
            Application application = application(1L, job(1L), ApplicationStatus.APPLIED);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

            Application result = applicationService.getApplicationById(1L);

            assertSame(application, result);
        }

        @Test
        @DisplayName("getApplicationById lancia ApplicationNotFoundException se non esiste")
        void throwsWhenNotFound() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ApplicationNotFoundException.class,
                    () -> applicationService.getApplicationById(99L));
        }
    }

    // ============================================================
    // updateApplication
    // ============================================================

    @Nested
    @DisplayName("updateApplication")
    class UpdateApplication {

        @Test
        @DisplayName("aggiorna stato e data senza controllare duplicati se il job non cambia")
        void updatesWithoutChangingJob() {
            Job job = job(1L);
            Application existing = application(10L, job, ApplicationStatus.APPLIED);
            ApplicationRequest request = request(1L, ApplicationStatus.INTERVIEW, LocalDate.of(2026, 10, 8));

            when(applicationRepository.findById(10L)).thenReturn(Optional.of(existing));

            Application result = applicationService.updateApplication(10L, request);

            assertEquals(ApplicationStatus.INTERVIEW, result.getStatus());
            assertEquals(LocalDate.of(2026, 10, 8), result.getAppliedAt());
            assertSame(job, result.getJob());

            // il job non cambia, quindi nessuna ricerca e nessun controllo duplicati
            verify(jobRepository, never()).findById(any());
            verify(applicationRepository, never()).existsByJobId(any());
        }

        @Test
        @DisplayName("cambia job se il nuovo job esiste e non ha gia' una candidatura")
        void updatesJobWhenFree() {
            Job oldJob = job(1L);
            Job newJob = job(2L);
            Application existing = application(10L, oldJob, ApplicationStatus.APPLIED);
            ApplicationRequest request = request(2L, ApplicationStatus.APPLIED, LocalDate.of(2026, 10, 8));

            when(applicationRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(jobRepository.findById(2L)).thenReturn(Optional.of(newJob));
            when(applicationRepository.existsByJobId(2L)).thenReturn(false);

            Application result = applicationService.updateApplication(10L, request);

            assertSame(newJob, result.getJob());
        }

        @Test
        @DisplayName("lancia DuplicateApplicationException se il nuovo job ha gia' una candidatura")
        void throwsWhenNewJobAlreadyHasApplication() {
            Job oldJob = job(1L);
            Job newJob = job(2L);
            Application existing = application(10L, oldJob, ApplicationStatus.APPLIED);
            ApplicationRequest request = request(2L, ApplicationStatus.APPLIED, LocalDate.now());

            when(applicationRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(jobRepository.findById(2L)).thenReturn(Optional.of(newJob));
            when(applicationRepository.existsByJobId(2L)).thenReturn(true);

            assertThrows(DuplicateApplicationException.class,
                    () -> applicationService.updateApplication(10L, request));

            // il job originale non deve essere stato modificato
            assertSame(oldJob, existing.getJob());
        }

        @Test
        @DisplayName("lancia JobNotFoundException se il nuovo job non esiste")
        void throwsWhenNewJobNotFound() {
            Application existing = application(10L, job(1L), ApplicationStatus.APPLIED);
            ApplicationRequest request = request(99L, ApplicationStatus.APPLIED, LocalDate.now());

            when(applicationRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(jobRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(JobNotFoundException.class,
                    () -> applicationService.updateApplication(10L, request));
        }

        @Test
        @DisplayName("lancia ApplicationNotFoundException se la candidatura non esiste")
        void throwsWhenApplicationNotFound() {
            ApplicationRequest request = request(1L, ApplicationStatus.APPLIED, LocalDate.now());
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ApplicationNotFoundException.class,
                    () -> applicationService.updateApplication(99L, request));

            verifyNoInteractions(jobRepository);
        }
    }

    // ============================================================
    // deleteApplication
    // ============================================================

    @Nested
    @DisplayName("deleteApplication")
    class DeleteApplication {

        @Test
        @DisplayName("elimina la candidatura se esiste")
        void deletes() {
            Application existing = application(10L, job(1L), ApplicationStatus.APPLIED);
            when(applicationRepository.findById(10L)).thenReturn(Optional.of(existing));

            applicationService.deleteApplication(10L);

            verify(applicationRepository).delete(existing);
        }

        @Test
        @DisplayName("lancia ApplicationNotFoundException e non elimina nulla se non esiste")
        void throwsWhenNotFound() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ApplicationNotFoundException.class,
                    () -> applicationService.deleteApplication(99L));

            verify(applicationRepository, never()).delete(any());
        }
    }
}