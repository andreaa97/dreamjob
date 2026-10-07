package com.andrea.remoteflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jobs")
public class Job {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String company;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private Long salaryMin;

    @Getter
    @Setter
    private Long salaryMax;

    @Getter
    @Setter
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