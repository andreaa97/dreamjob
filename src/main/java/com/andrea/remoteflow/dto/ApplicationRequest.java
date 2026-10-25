package com.andrea.remoteflow.dto;

import com.andrea.remoteflow.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Dati in ingresso per creare o aggiornare una candidatura.
 * <p>
 * E' un record: immutabile, con costruttore e accessor generati ({@code jobId()}, ...).
 * Le annotazioni di validazione vengono controllate da {@code @Valid} nel controller.
 *
 * @param jobId     il job a cui ci si candida (obbligatorio)
 * @param status    lo stato della candidatura (obbligatorio); un valore che non esiste
 *                  nell'enum fa fallire la lettura del JSON con 400
 * @param appliedAt data della candidatura nel formato yyyy-MM-dd (facoltativa)
 */
public record ApplicationRequest(
        @NotNull Long jobId,
        @NotNull ApplicationStatus status,
        LocalDate appliedAt
) {
}
