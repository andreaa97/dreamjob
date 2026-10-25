package com.andrea.remoteflow.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // A database constraint was violated (e.g. two requests for the same job passing the
    // service check at the same time, then hitting uq_applications_job): 409, not 500.
    // sendError keeps the same error body as the @ResponseStatus exceptions.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleDataIntegrityViolation(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value());
    }
}
