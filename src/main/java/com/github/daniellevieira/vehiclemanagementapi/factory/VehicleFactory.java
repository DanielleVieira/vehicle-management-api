package com.github.daniellevieira.vehiclemanagementapi.factory;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleRequest;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleFactory {

    public Vehicle create(VehicleRequest vehicleRequest, Client owner) {
        return new Vehicle(
                vehicleRequest.make(),
                vehicleRequest.model(),
                vehicleRequest.year(),
                vehicleRequest.licensePlate(),
                owner
        );
    }
}
