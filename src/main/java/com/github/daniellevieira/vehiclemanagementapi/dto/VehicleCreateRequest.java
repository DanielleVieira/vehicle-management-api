package com.github.daniellevieira.vehiclemanagementapi.dto;

public record VehicleCreateRequest(
        String make,
        String model,
        int year,
        String licensePlate,
        long ownerId // ver como fica pra pegar só um ID existente
) {
}
