package com.github.daniellevieira.vehiclemanagementapi.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record ClientCreateRequest(
        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 100, message = "size must be between 3 and 100")
        String name,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "size must be between 0 and 255")
        @Email(message = "must be a well-formatted email address")
        String email,

        @NotBlank(message = "must not be blank")
        @CPF(message = "invalid Brazilian individual taxpayer registry number (CPF)")
        String cpf,

        @NotNull(message = "must not be null")
        @Past(message = "must be a past date")
        LocalDate birthDate
) {
}
