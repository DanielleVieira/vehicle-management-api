package com.github.daniellevieira.vehiclemanagementapi.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
