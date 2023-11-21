package com.example.test.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        Throwable rootCause = exception.getMostSpecificCause();
        if (rootCause instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) rootCause;
            if ("uk_author_title".equals(cve.getConstraintName())) {
                return createExceptionDto("A book with this author and title already exists.");
            }
            if ("uk_email".equals(cve.getConstraintName())) {
                return new ExceptionDto("Customer with the given email already exists.");
            }
            if ("uk_subscription_customer_author_category".equals(cve.getConstraintName())) {
                return new ExceptionDto("Subscription with these details already exists for the customer");
            }
        }
        return createExceptionDto("A database error occurred. Please try again later.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleResourceNotFoundException(ResourceNotFoundException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleDuplicateResourceException(DuplicateResourceException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleDatabaseException(DatabaseException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleGeneralException(Exception exception) {
        return createExceptionDto("An unexpected error occurred. Please try again later.");
    }

    @ExceptionHandler(EmailServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleEmailServiceException(EmailServiceException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBusinessException(BusinessException exception) {
        return createExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(OperationFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleOperationFailedException(OperationFailedException exception) {
        return createExceptionDto(exception.getMessage());
    }

    private ExceptionDto createExceptionDto(String message) {
        return new ExceptionDto(message);
    }
}