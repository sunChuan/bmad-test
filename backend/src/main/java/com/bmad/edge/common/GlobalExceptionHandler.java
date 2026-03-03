package com.bmad.edge.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        log.warn("Access denied: ", e);
        return Result.error(403, "Access Denied: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: ", e);
        return Result.error(400, "Bad Request: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("System error: ", e);
        // Ensure HTTP 200 is always returned per architecture guidelines
        return Result.error(500, "Internal Server Error: " + e.getMessage());
    }
}
