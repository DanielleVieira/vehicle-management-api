package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

public record LoginResponse(
        String token,
        String type,
        UserResponse userResponse
) {
}
