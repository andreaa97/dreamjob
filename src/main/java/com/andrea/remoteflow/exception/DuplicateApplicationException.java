package com.andrea.remoteflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Esiste gia' una candidatura per questo job (regola: una sola candidatura per job).
 * Risposta HTTP: 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(Long jobId) {
        super("Application already exists for job with id: " + jobId);
    }
}
