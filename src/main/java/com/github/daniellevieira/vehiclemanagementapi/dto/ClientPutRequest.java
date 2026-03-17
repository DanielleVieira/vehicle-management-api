package com.github.daniellevieira.vehiclemanagementapi.dto;

import java.time.LocalDate;

public record ClientPutRequest(
        String name,
        String email,
        String cpf,
        LocalDate birthDate
) {
}
