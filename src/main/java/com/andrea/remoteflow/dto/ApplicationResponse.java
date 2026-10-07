package com.andrea.remoteflow.dto;

import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.ApplicationStatus;

import java.time.LocalDate;

public record ApplicationResponse(Long id, Long jobId, String jobTitle,
                                  ApplicationStatus status, LocalDate appliedAt) {
    public static ApplicationResponse from(Application a) {
        return new ApplicationResponse(a.getId(), a.getJob().getId(),
                a.getJob().getTitle(), a.getStatus(), a.getAppliedAt());
    }
}