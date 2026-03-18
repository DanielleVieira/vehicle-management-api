package com.github.daniellevieira.vehiclemanagementapi.dto;

public interface VehicleRequest {
    String make();

    String model();

    int year();

    String licensePlate();
}
