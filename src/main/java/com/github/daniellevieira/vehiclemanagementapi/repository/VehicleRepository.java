package com.github.daniellevieira.vehiclemanagementapi.repository;

import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findAllByOwner(Client owner);
}
