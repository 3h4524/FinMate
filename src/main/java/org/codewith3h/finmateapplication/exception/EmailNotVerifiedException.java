package org.codewith3h.finmateapplication.exception;
 
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
} 