package com.github.daniellevieira.vehiclemanagementapi.factory;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleDTO;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleFactory {

    public Vehicle create(VehicleDTO vehicleDTO, Client owner) {
        return new Vehicle(
                vehicleDTO.make(),
                vehicleDTO.model(),
                vehicleDTO.year(),
                vehicleDTO.licensePlate(),
                owner
        );
    }
}
