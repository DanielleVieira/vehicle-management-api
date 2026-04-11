package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.Role;

public record UserPutRequest(
        String email,
        String password
) { }
