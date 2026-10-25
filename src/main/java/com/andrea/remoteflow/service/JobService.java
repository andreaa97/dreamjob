package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.JobRequest;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.JobHasApplicationException;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica di business dei job (annunci di lavoro): creazione, modifica, ricerca con filtri
 * ed eliminazione. La validazione dei dati e' gia' avvenuta nel controller ({@code @Valid}).
 */
@Service
public class JobService {

    private final JobRepository jobRepository;
    // serve solo per sapere se un job ha candidature prima di eliminarlo
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository,
                      ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    /** Crea un job e lo restituisce con l'id assegnato dal database. */
    @Transactional
    public Job createJob(JobRequest request) {
        Job job = new Job(
                request.title(),
                request.company(),
                request.location(),
                request.salaryMin(),
                request.salaryMax(),
                request.remoteFromItaly()
        );
        return jobRepository.save(job);
    }

    /**
     * Sostituisce tutti i campi di un job.
     *
     * @throws JobNotFoundException se il job non esiste (404)
     */
    @Transactional
    public Job updateJob(Long id, JobRequest request) {
        Job job = getJobById(id);

        job.setTitle(request.title());
        job.setCompany(request.company());
        job.setLocation(request.location());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setRemoteFromItaly(request.remoteFromItaly());

        // nessun save(): entita' gestita, Hibernate scrive le modifiche al commit
        return job;
    }

    /**
     * Un job per id.
     *
     * @throws JobNotFoundException se non esiste (404)
     */
    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    /**
     * Elimina un job che non ha candidature.
     *
     * @throws JobNotFoundException        se il job non esiste (404)
     * @throws JobHasApplicationException  se ha una candidatura (409)
     */
    @Transactional
    public void deleteJob(Long id) {
        Job job = getJobById(id);

        // applications.job_id punta al job: eliminarlo violerebbe la foreign key (errore 500).
        // Lo controlliamo prima per rispondere con un 409 e un messaggio chiaro.
        if (applicationRepository.existsByJobId(id)) {
            throw new JobHasApplicationException(id);
        }

        jobRepository.delete(job);
    }

    /**
     * Cerca i job con filtri facoltativi: un parametro {@code null} significa "qualsiasi".
     *
     * @param remoteFromItaly se valorizzato, solo i job remoti (true) o non remoti (false)
     * @param salaryMin       se valorizzato, solo i job con stipendio minimo maggiore o uguale
     */
    @Transactional(readOnly = true)
    public List<Job> search(Boolean remoteFromItaly, Long salaryMin) {
        if (remoteFromItaly != null && salaryMin != null)
            return jobRepository.findByRemoteFromItalyAndSalaryMinGreaterThanEqual(remoteFromItaly, salaryMin);
        if (remoteFromItaly != null) return jobRepository.findByRemoteFromItaly(remoteFromItaly);
        if (salaryMin != null) return jobRepository.findBySalaryMinGreaterThanEqual(salaryMin);
        return jobRepository.findAll();
    }
}
