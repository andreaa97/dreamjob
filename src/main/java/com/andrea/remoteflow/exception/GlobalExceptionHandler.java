package com.andrea.remoteflow.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * Gestione centralizzata delle eccezioni che non hanno gia' un {@code @ResponseStatus}.
 * {@code @RestControllerAdvice} applica questi handler a tutti i controller.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Il database ha rifiutato un'operazione per un vincolo violato. Succede, per esempio,
     * quando due richieste per lo stesso job superano insieme il controllo nel service e la
     * seconda viene bloccata da {@code uq_applications_job}: rispondiamo 409 invece di 500.
     * {@code sendError} produce lo stesso corpo JSON delle eccezioni con {@code @ResponseStatus}.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleDataIntegrityViolation(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value());
    }
}
