package org.codewith3h.finmateapplication.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handlingRuntimeException(RuntimeException e) {
        logger.error("Runtime exception occurred: ", e);
        return ResponseEntity.badRequest().body(Map.of(
            "error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlingException(Exception e) {
        logger.error("Unexpected exception occurred: ", e);
        return ResponseEntity.internalServerError().body(Map.of(
            "error", "An unexpected error occurred"
        ));
    }
}
