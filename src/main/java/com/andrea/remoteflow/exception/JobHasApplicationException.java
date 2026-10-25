package com.andrea.remoteflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JobHasApplicationException extends RuntimeException {

    public JobHasApplicationException(Long jobId) {
        super("Job with id: " + jobId + " has an application and cannot be deleted");
    }
}
