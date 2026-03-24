package com.github.daniellevieira.vehiclemanagementapi.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record ClientPutRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @NotBlank
        @Size(max = 255)
        @Email
        String email,

        @NotBlank
        @CPF
        String cpf,

        @NotNull
        @Past
        LocalDate birthDate
) {
}
