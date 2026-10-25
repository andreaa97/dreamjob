package com.andrea.remoteflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di avvio dell'applicazione Spring Boot.
 * <p>
 * {@code @SpringBootApplication} attiva la configurazione automatica e la scansione dei
 * componenti in questo package e nei sottopackage (controller, service, repository...).
 * All'avvio Flyway applica le migrazioni in {@code db/migration} prima che Hibernate parta.
 */
@SpringBootApplication
public class DreamJob {

	public static void main(String[] args) {
		SpringApplication.run(DreamJob.class, args);
	}

}
