package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.Role;

public record UserResponse(
        Long id,
        String email,
        Role role
) {
}
