package com.andrea.remoteflow.controller;

import com.andrea.remoteflow.dto.JobRequest;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST dei job (annunci di lavoro): {@code /api/jobs}.
 * <p>
 * Il body delle richieste e' un {@link JobRequest} validato con {@code @Valid}: se un vincolo
 * non e' rispettato Spring risponde 400 senza arrivare al service.
 * Le risposte restituiscono direttamente l'entita' {@link Job}: non ha relazioni da caricare,
 * quindi la serializzazione in JSON e' sicura.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** Crea un job. Risponde 201 Created con il job salvato (id compreso). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Job createJob(@Valid @RequestBody JobRequest request) {
        return jobService.createJob(request);
    }

    /**
     * Cerca i job. Entrambi i filtri sono facoltativi: se un parametro manca, non filtra
     * su quel campo (es. {@code GET /api/jobs?remoteFromItaly=true&salaryMin=40000}).
     */
    @GetMapping
    public List<Job> getJobs(@RequestParam(required = false) Boolean remoteFromItaly,
                             @RequestParam(required = false) Long salaryMin) {
        return jobService.search(remoteFromItaly, salaryMin);
    }

    /** Restituisce un job, oppure 404 se non esiste. */
    @GetMapping("/{id}")
    public Job getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    /** Sostituisce tutti i campi di un job (PUT: dati completi, validati come nella creazione). */
    @PutMapping("/{id}")
    public Job updateJob(@PathVariable Long id,
                         @Valid @RequestBody JobRequest request) {
        return jobService.updateJob(id, request);
    }

    /** Elimina un job: 204 se riuscito, 409 se ha ancora una candidatura, 404 se non esiste. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }
}
