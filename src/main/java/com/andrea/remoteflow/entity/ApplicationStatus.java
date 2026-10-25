package com.andrea.remoteflow.entity;

/**
 * Stati possibili di una candidatura, nell'ordine in cui di solito si susseguono.
 * Il frontend usa gli stessi valori (vedi {@code models.ts}).
 */
public enum ApplicationStatus {
    /** Annuncio salvato, candidatura non ancora inviata. */
    SAVED,
    /** Candidatura inviata. */
    APPLIED,
    /** In fase di colloquio. */
    INTERVIEW,
    /** Offerta ricevuta. */
    OFFER,
    /** Rifiutata dall'azienda. */
    REJECTED,
    /** Ritirata dal candidato. */
    WITHDRAWN
}
