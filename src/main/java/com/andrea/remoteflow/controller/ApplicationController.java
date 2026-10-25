package com.andrea.remoteflow.controller;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.dto.ApplicationResponse;
import com.andrea.remoteflow.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST delle candidature: {@code /api/applications}.
 * <p>
 * Il controller resta sottile: valida l'input ({@code @Valid}), delega la logica a
 * {@link ApplicationService} e converte le entita' in {@link ApplicationResponse}, cosi' il
 * JSON non espone l'entita' JPA (e il job collegato) cosi' com'e'.
 * Gli errori diventano codici HTTP tramite le eccezioni del package {@code exception}:
 * 400 dati non validi, 404 candidatura o job inesistente, 409 candidatura duplicata.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    // iniezione tramite costruttore: Spring passa il service, il campo puo' essere final
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Crea una candidatura per un job esistente. Risponde 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(@Valid @RequestBody ApplicationRequest request) {
        return ApplicationResponse.from(applicationService.createApplication(request));
    }

    /** Restituisce tutte le candidature, ognuna con id e titolo del job. */
    @GetMapping
    public List<ApplicationResponse> getApplications() {
        return applicationService.getAllApplications().stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    /** Restituisce una candidatura, oppure 404 se non esiste. */
    @GetMapping("/{id}")
    public ApplicationResponse getApplicationById(@PathVariable Long id) {
        return ApplicationResponse.from(applicationService.getApplicationById(id));
    }

    /** Aggiorna stato, data ed eventualmente il job di una candidatura (PUT: dati completi). */
    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(@PathVariable Long id,
                                                 @Valid @RequestBody ApplicationRequest request) {
        return ApplicationResponse.from(applicationService.updateApplication(id, request));
    }

    /** Elimina una candidatura; il job collegato resta. Risponde 204 No Content. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
    }
}
