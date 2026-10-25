package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.ApplicationNotFoundException;
import com.andrea.remoteflow.exception.DuplicateApplicationException;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica di business delle candidature.
 * <p>
 * Regola principale: <b>al massimo una candidatura per job</b>. Il service la controlla per
 * dare un errore chiaro (409); il vincolo {@code uq_applications_job} nel database la
 * garantisce anche quando due richieste arrivano nello stesso istante.
 * <p>
 * Ogni metodo pubblico gira in una transazione ({@code @Transactional}): se qualcosa fallisce
 * a meta', nessuna modifica viene salvata. Le letture usano {@code readOnly = true}, che
 * permette a Hibernate di evitare il controllo delle modifiche.
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Crea una candidatura.
     *
     * @throws JobNotFoundException          se il job indicato non esiste (404)
     * @throws DuplicateApplicationException se il job ha gia' una candidatura (409)
     */
    @Transactional
    public Application createApplication(ApplicationRequest request) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new JobNotFoundException(request.jobId()));

        if (applicationRepository.existsByJobId(job.getId())) {
            throw new DuplicateApplicationException(job.getId());
        }

        Application application = new Application();
        application.setJob(job);
        application.setStatus(request.status());
        application.setAppliedAt(request.appliedAt());
        return applicationRepository.save(application);
    }

    /** Tutte le candidature, con il job gia' caricato (vedi {@code ApplicationRepository}). */
    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    /**
     * Una candidatura per id.
     *
     * @throws ApplicationNotFoundException se non esiste (404)
     */
    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    /**
     * Aggiorna stato, data ed eventualmente il job di una candidatura.
     *
     * @throws ApplicationNotFoundException  se la candidatura non esiste (404)
     * @throws JobNotFoundException          se il nuovo job non esiste (404)
     * @throws DuplicateApplicationException se il nuovo job ha gia' una candidatura (409)
     */
    @Transactional
    public Application updateApplication(Long id, ApplicationRequest request) {
        Application application = getApplicationById(id);

        // la regola dei duplicati va ricontrollata solo se la candidatura cambia job
        if (!application.getJob().getId().equals(request.jobId())) {
            Job job = jobRepository.findById(request.jobId())
                    .orElseThrow(() -> new JobNotFoundException(request.jobId()));
            if (applicationRepository.existsByJobId(job.getId())) {
                throw new DuplicateApplicationException(job.getId());
            }
            application.setJob(job);
        }

        application.setStatus(request.status());
        application.setAppliedAt(request.appliedAt());
        // nessun save(): l'entita' e' "gestita" da Hibernate dentro la transazione,
        // quindi le modifiche vengono scritte nel database al commit (dirty checking)
        return application;
    }

    /**
     * Elimina una candidatura; il job resta.
     *
     * @throws ApplicationNotFoundException se non esiste (404)
     */
    @Transactional
    public void deleteApplication(Long id) {
        applicationRepository.delete(getApplicationById(id));
    }
}
