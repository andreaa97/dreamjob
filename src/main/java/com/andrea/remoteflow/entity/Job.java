package com.andrea.remoteflow.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String title;

    private String company;

    private String location;

    private Long salaryMin;

    private Long salaryMax;

    private Boolean remoteFromItaly;

    public Job() {
    }

    public Job(
            String title,
            String company,
            String location,
            Long salaryMin,
            Long salaryMax,
            Boolean remoteFromItaly
    ) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.remoteFromItaly = remoteFromItaly;
    }

}
