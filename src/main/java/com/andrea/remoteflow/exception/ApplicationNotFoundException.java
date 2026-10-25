package com.andrea.remoteflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * La candidatura richiesta non esiste.
 * {@code @ResponseStatus} fa rispondere Spring con 404 quando l'eccezione esce dal controller.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("Application not found with id: " + id);
    }
}
