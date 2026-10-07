package com.andrea.remoteflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class JobRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String company;

    private String location;

    @PositiveOrZero
    private Long salaryMin;

    @PositiveOrZero
    private Long salaryMax;

    @NotNull
    private Boolean remoteFromItaly;

    @JsonIgnore
    @AssertTrue(message = "salaryMax must be greater than or equal to salaryMin")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax >= salaryMin;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Long salaryMin) { this.salaryMin = salaryMin; }

    public Long getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Long salaryMax) { this.salaryMax = salaryMax; }

    public Boolean getRemoteFromItaly() { return remoteFromItaly; }
    public void setRemoteFromItaly(Boolean remoteFromItaly) { this.remoteFromItaly = remoteFromItaly; }
}