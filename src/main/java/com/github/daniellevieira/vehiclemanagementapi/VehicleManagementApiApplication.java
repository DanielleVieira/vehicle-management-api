package com.github.daniellevieira.vehiclemanagementapi;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.mapper.ClientMapper;
import com.github.daniellevieira.vehiclemanagementapi.mapper.VehicleMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class VehicleManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleManagementApiApplication.class, args);
    }

}
