package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // TODO handle para quando o vehicle ou campos vem nulo ou incorretos e quando clientId é incorreto
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@RequestBody VehicleCreateRequest vehicleCreateRequest) {
        var vehicleResponse = vehicleService.createVehicle(vehicleCreateRequest);
        var location = UriUtils.createLocationFromCurrentRequest(Long.toString(vehicleResponse.id()));
        return ResponseEntity.created(location).body(vehicleResponse);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // TODO handle de veículo não encontrado, id inválido ou nulo
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@RequestParam Long id) {
        return ResponseEntity.ok(vehicleService.getVehicle(id));
    }

    // TODO handle de id nulo ou inválido
    @DeleteMapping
    public ResponseEntity<VehicleResponse> deleteVehicle(@RequestParam Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

}
