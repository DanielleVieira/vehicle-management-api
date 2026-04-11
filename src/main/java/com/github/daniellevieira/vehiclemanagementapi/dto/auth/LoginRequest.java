package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
