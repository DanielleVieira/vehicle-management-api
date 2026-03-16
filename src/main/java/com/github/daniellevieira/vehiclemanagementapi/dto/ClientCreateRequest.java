package com.github.daniellevieira.vehiclemanagementapi.dto;

import java.time.LocalDate;

public record ClientCreateRequest(
        String name,
        String email,
        String cpf,
        LocalDate birthDate
) {
}
