package com.github.daniellevieira.vehiclemanagementapi.dto;

import com.github.daniellevieira.vehiclemanagementapi.model.Client;

public record VehicleResponse(
        long id,
        String make,
        String model,
        int year,
        String licensePlate,
        Client owner
) {
}
