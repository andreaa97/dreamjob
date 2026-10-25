package com.andrea.remoteflow.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Annuncio di lavoro: tabella {@code jobs} (creata dalla migrazione V1).
 * <p>
 * I nomi dei campi in camelCase corrispondono alle colonne in snake_case
 * (es. {@code salaryMin} -> {@code salary_min}) grazie alla naming strategy di Spring.
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {

    // generato dal database: nessun setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String title;

    private String company;

    private String location;

    // stipendio annuo in euro; null se l'annuncio non lo indica
    private Long salaryMin;

    private Long salaryMax;

    private Boolean remoteFromItaly;

    // costruttore vuoto richiesto da JPA
    public Job() {
    }

    // usato dal service per creare un nuovo job (l'id lo assegna il database)
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
