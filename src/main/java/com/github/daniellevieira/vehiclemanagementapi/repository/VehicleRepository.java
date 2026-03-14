package com.github.daniellevieira.vehiclemanagementapi.repository;

import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
