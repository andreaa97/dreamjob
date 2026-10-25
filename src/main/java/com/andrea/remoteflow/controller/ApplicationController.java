package com.andrea.remoteflow.controller;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.dto.ApplicationResponse;
import com.andrea.remoteflow.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(@Valid @RequestBody ApplicationRequest request) {
        return ApplicationResponse.from(applicationService.createApplication(request));
    }

    @GetMapping
    public List<ApplicationResponse> getApplications() {
        return applicationService.getAllApplications().stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationById(@PathVariable Long id) {
        return ApplicationResponse.from(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(@PathVariable Long id,
                                                 @Valid @RequestBody ApplicationRequest request) {
        return ApplicationResponse.from(applicationService.updateApplication(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
    }
}