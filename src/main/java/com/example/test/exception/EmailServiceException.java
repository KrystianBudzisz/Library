package com.example.test.exception;

public class EmailServiceException extends RuntimeException {
    public EmailServiceException(String message, Exception e) {
        super(message, e);
    }
    public EmailServiceException(String message) {
        super(message);
    }
}
