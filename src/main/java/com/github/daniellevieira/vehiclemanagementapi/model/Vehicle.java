package com.github.daniellevieira.vehiclemanagementapi.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 50)
    private String make;
    @Column(nullable = false, length = 100)
    private String model;
    @Column(name = "vehicle_year", nullable = false, length = 4)
    private int year;
    @Column(nullable = false, unique = true, length = 7)
    private String licensePlate;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client owner;

    protected Vehicle() {
    }

    public Vehicle(String make, String model, int year, String licensePlate, Client owner) {
        setVehicleProperties(this, this.id, make, model, year, licensePlate, owner);
    }

    public Vehicle updateVehicle(
            String newMake,
            String newModel,
            int newYear,
            String newLicensePlate
    ) {
        return setVehicleProperties(new Vehicle(), this.id, newMake, newModel, newYear, newLicensePlate, this.owner);
    }

    private Vehicle setVehicleProperties(
            Vehicle vehicle,
            Long id,
            String make,
            String model,
            int year,
            String licensePlate,
            Client owner
    ) {
        vehicle.id = id;
        vehicle.make = Objects.requireNonNull(make)
                .trim()
                .toUpperCase();
        vehicle.model = Objects.requireNonNull(model)
                .trim()
                .toUpperCase();
        vehicle.year = year;
        vehicle.licensePlate = Objects.requireNonNull(licensePlate)
                .trim()
                .toUpperCase()
                .replace("-", "");
        vehicle.owner = Objects.requireNonNull(owner);
        return vehicle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return id == vehicle.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", licensePlate='" + licensePlate + '\'' +
                ", owner=" + owner +
                '}';
    }
}
