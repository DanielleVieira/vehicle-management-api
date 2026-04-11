package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.Role;

public record UserCreateRequest(
        String email,
        String password
) { }
