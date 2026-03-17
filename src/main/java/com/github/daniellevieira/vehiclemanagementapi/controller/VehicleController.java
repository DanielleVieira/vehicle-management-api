package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
