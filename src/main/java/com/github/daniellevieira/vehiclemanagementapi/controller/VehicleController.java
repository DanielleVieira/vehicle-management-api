package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/vehicle")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // TODO lembrar de tratar no handle o caso de owner não encontrado
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@RequestBody VehicleCreateRequest vehicleCreateRequest) {
        var vehicleResponse = vehicleService.createVehicle(vehicleCreateRequest);
        URI location = createUriFromId(Long.toString(vehicleResponse.id()));
        return ResponseEntity.created(location).body(vehicleResponse);
    }

    private URI createUriFromId(String id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }


}
