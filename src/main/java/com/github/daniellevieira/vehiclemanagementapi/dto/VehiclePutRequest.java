package com.github.daniellevieira.vehiclemanagementapi.dto;

public record VehiclePutRequest(
        String make,
        String model,
        int year,
        String licensePlate
) implements VehicleRequest { }
