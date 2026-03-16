package com.github.daniellevieira.vehiclemanagementapi.dto;

import java.time.LocalDate;

public record ClientResponse(
        long id,
        String name,
        String email,
        String cpf,
        LocalDate birthDate
) {
}
