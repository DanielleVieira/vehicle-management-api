package com.github.daniellevieira.vehiclemanagementapi.dto;

public record VehicleResponse(
        long id,
        String make,
        String model,
        int year,
        String licensePlate,
        ClientResponse owner
) {
}
