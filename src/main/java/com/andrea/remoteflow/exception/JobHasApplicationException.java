package com.andrea.remoteflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Il job non si puo' eliminare perche' ha una candidatura (la foreign key
 * {@code applications.job_id} lo impedirebbe comunque). Risposta HTTP: 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class JobHasApplicationException extends RuntimeException {

    public JobHasApplicationException(Long jobId) {
        super("Job with id: " + jobId + " has an application and cannot be deleted");
    }
}
