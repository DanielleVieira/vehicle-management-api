package com.github.daniellevieira.vehiclemanagementapi.exception;

public class ResourceNotFoundException  extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
