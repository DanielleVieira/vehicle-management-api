package com.github.daniellevieira.vehiclemanagementapi.dto.auth;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPutRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "size must be between 0 and 255")
        @Email(message = "must be a well-formatted email address")
        String email,

        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 72, message = "size must be between 8 and 72") // 72 é o limite usado pelo Bcrypt
        String password
) { }
