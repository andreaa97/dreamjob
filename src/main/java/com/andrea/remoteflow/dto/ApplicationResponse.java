package com.andrea.remoteflow.dto;

import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.ApplicationStatus;

import java.time.LocalDate;

/**
 * Candidatura come viene restituita dall'API.
 * <p>
 * Del job espone solo id e titolo ("appiattiti" in {@code jobId} e {@code jobTitle}):
 * il client ha quello che gli serve senza ricevere l'intera entita' collegata.
 */
public record ApplicationResponse(Long id, Long jobId, String jobTitle,
                                  ApplicationStatus status, LocalDate appliedAt) {

    /**
     * Converte l'entita' nella risposta. Legge il titolo del job: per questo il repository
     * carica il job insieme alla candidatura (vedi {@code ApplicationRepository}).
     */
    public static ApplicationResponse from(Application a) {
        return new ApplicationResponse(a.getId(), a.getJob().getId(),
                a.getJob().getTitle(), a.getStatus(), a.getAppliedAt());
    }
}
