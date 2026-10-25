package com.andrea.remoteflow.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Candidatura a un job: tabella {@code applications} (creata dalla migrazione V1).
 * <p>
 * Lombok genera getter e setter; l'id non ha setter perche' lo assegna il database.
 */
@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {

    // IDENTITY: l'id viene generato dal database al momento dell'INSERT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // salvato come testo ("APPLIED", ...) e non come numero: aggiungere o riordinare
    // gli stati dell'enum non corrompe i dati gia' presenti
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDate appliedAt;

    // LAZY: il job non viene letto finche' non serve. Quando serve sempre (le risposte API),
    // ApplicationRepository lo carica nella stessa query con @EntityGraph.
    // unique = true: al massimo una candidatura per job (vincolo uq_applications_job).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", unique = true)
    private Job job;

    // costruttore vuoto richiesto da JPA
    public Application() {
    }

}
