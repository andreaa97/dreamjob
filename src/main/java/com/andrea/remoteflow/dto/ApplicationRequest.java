package com.andrea.remoteflow.dto;

import com.andrea.remoteflow.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ApplicationRequest {

    @NotNull
    private Long jobId;

    @NotNull
    private ApplicationStatus status;

    private LocalDate appliedAt;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDate appliedAt) {
        this.appliedAt = appliedAt;
    }
}