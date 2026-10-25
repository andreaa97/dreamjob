package com.andrea.remoteflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record JobRequest(
        @NotBlank String title,
        @NotBlank String company,
        String location,
        @PositiveOrZero Long salaryMin,
        @PositiveOrZero Long salaryMax,
        @NotNull Boolean remoteFromItaly
) {

    @JsonIgnore
    @AssertTrue(message = "salaryMax must be greater than or equal to salaryMin")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax >= salaryMin;
    }
}
