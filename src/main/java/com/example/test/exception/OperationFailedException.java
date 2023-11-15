package com.example.test.exception;

public class OperationFailedException extends RuntimeException {
    public OperationFailedException(String message, Exception e) {
        super(message, e);
    }
}
