package com.andrea.remoteflow.dto;

import com.andrea.remoteflow.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApplicationRequest(
        @NotNull Long jobId,
        @NotNull ApplicationStatus status,
        LocalDate appliedAt
) {
}
