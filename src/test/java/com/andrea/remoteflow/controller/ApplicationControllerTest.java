package com.andrea.remoteflow.controller;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.ApplicationStatus;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.ApplicationNotFoundException;
import com.andrea.remoteflow.service.ApplicationService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;


    private Job job(Long id) {

        Job job = new Job(
                "Backend Developer",
                "Acme",
                "Remote",
                45000L,
                60000L,
                true
        );

        ReflectionTestUtils.setField(job, "id", id);

        return job;
    }


    private Application application(
            Long id,
            Job job,
            ApplicationStatus status,
            LocalDate appliedAt
    ) {

        Application application = new Application();

        ReflectionTestUtils.setField(application, "id", id);

        application.setJob(job);
        application.setStatus(status);
        application.setAppliedAt(appliedAt);

        return application;
    }


    @Test
    @DisplayName("POST /api/applications restituisce 201 e crea la candidatura")
    void createApplication() throws Exception {

        Job job = job(1L);

        Application savedApplication = application(
                10L,
                job,
                ApplicationStatus.APPLIED,
                LocalDate.of(2026, 10, 7)
        );

        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenReturn(savedApplication);


        mockMvc.perform(
                        post("/api/applications")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "jobId": 1,
                                          "status": "APPLIED",
                                          "appliedAt": "2026-10-07"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.appliedAt").value("2026-10-07"))
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));


        verify(applicationService)
                .createApplication(any(ApplicationRequest.class));
    }


    @Test
    @DisplayName("GET /api/applications restituisce tutte le candidature")
    void getAllApplications() throws Exception {

        Application first = application(
                1L,
                job(1L),
                ApplicationStatus.APPLIED,
                LocalDate.of(2026, 10, 1)
        );

        Application second = application(
                2L,
                job(2L),
                ApplicationStatus.INTERVIEW,
                LocalDate.of(2026, 10, 2)
        );

        when(applicationService.getAllApplications())
                .thenReturn(List.of(first, second));


        mockMvc.perform(
                        get("/api/applications")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("APPLIED"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("INTERVIEW"));


        verify(applicationService).getAllApplications();
    }


    @Test
    @DisplayName("GET /api/applications/{id} restituisce la candidatura")
    void getApplicationById() throws Exception {

        Application application = application(
                10L,
                job(1L),
                ApplicationStatus.INTERVIEW,
                LocalDate.of(2026, 10, 5)
        );

        when(applicationService.getApplicationById(10L))
                .thenReturn(application);


        mockMvc.perform(
                        get("/api/applications/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("INTERVIEW"))
                .andExpect(jsonPath("$.jobId").value(1));


        verify(applicationService).getApplicationById(10L);
    }


    @Test
    @DisplayName("PUT /api/applications/{id} aggiorna la candidatura")
    void updateApplication() throws Exception {

        Application updated = application(
                10L,
                job(1L),
                ApplicationStatus.INTERVIEW,
                LocalDate.of(2026, 10, 8)
        );

        when(applicationService.updateApplication(
                eq(10L),
                any(ApplicationRequest.class)
        )).thenReturn(updated);


        mockMvc.perform(
                        put("/api/applications/10")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "jobId": 1,
                                          "status": "INTERVIEW",
                                          "appliedAt": "2026-10-08"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("INTERVIEW"))
                .andExpect(jsonPath("$.appliedAt").value("2026-10-08"))
                .andExpect(jsonPath("$.jobId").value(1));


        verify(applicationService)
                .updateApplication(
                        eq(10L),
                        any(ApplicationRequest.class)
                );
    }


    @Test
    @DisplayName("DELETE /api/applications/{id} elimina la candidatura")
    void deleteApplication() throws Exception {

        doNothing()
                .when(applicationService)
                .deleteApplication(10L);


        mockMvc.perform(
                        delete("/api/applications/10")
                )
                .andExpect(status().isNoContent());


        verify(applicationService).deleteApplication(10L);
    }


    @Test
    @DisplayName("POST restituisce 400 se status non è valido")
    void createApplicationWithInvalidStatus() throws Exception {

        mockMvc.perform(
                        post("/api/applications")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "jobId": 1,
                                          "status": "NON_ESISTE",
                                          "appliedAt": "2026-10-07"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());


        verifyNoInteractions(applicationService);
    }


    @Test
    @DisplayName("POST restituisce 400 se la data non è valida")
    void createApplicationWithInvalidDate() throws Exception {

        mockMvc.perform(
                        post("/api/applications")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "jobId": 1,
                                          "status": "APPLIED",
                                          "appliedAt": "07-10-2026"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());


        verifyNoInteractions(applicationService);
    }


    @Test
    @DisplayName("GET restituisce 404 se la candidatura non esiste")
    void getApplicationNotFound() throws Exception {

        when(applicationService.getApplicationById(99L))
                .thenThrow(new ApplicationNotFoundException(99L));


        mockMvc.perform(
                        get("/api/applications/99")
                )
                .andExpect(status().isNotFound());


        verify(applicationService).getApplicationById(99L);
    }


    @Test
    @DisplayName("POST restituisce 409 se il database rifiuta la candidatura (vincolo violato)")
    void createApplicationConstraintViolation() throws Exception {

        // es. due richieste per lo stesso job arrivate insieme: il controllo nel service
        // passa per entrambe, ma il vincolo uq_applications_job blocca la seconda
        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenThrow(new DataIntegrityViolationException("uq_applications_job"));


        mockMvc.perform(
                        post("/api/applications")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "jobId": 1,
                                          "status": "APPLIED",
                                          "appliedAt": "2026-10-07"
                                        }
                                        """)
                )
                .andExpect(status().isConflict());
    }
}