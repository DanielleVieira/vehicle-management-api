package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehiclePutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("api/v1/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid
            @RequestBody
            VehicleCreateRequest vehicleCreateRequest
    ) {
        var vehicleResponse = vehicleService.createVehicle(vehicleCreateRequest);
        var location = UriUtils.createLocationFromCurrentRequest(Long.toString(vehicleResponse.id()));
        return ResponseEntity.created(location).body(vehicleResponse);
    }

    // TODO testes e handle para o caso de PropertyReferenceException do pageable
    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false)
            @Positive
            Long ownerId,
            @PageableDefault(page = 0, size = 10, sort = "model", direction = Sort.Direction.ASC)
            @ParameterObject
            Pageable pageable
    ) {
        if (ownerId != null) {
            return ResponseEntity.ok(vehicleService.getVehiclesByOwnerId(ownerId, pageable));
        }
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicle(
            @PathVariable
            @NotNull
            @Positive
            Long vehicleId
    ) {
        return ResponseEntity.ok(vehicleService.getVehicle(vehicleId));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> deleteVehicle(
            @PathVariable
            @NotNull
            @Positive
            Long vehicleId
    ) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }

    // Não permite atualizar o owner, apenas dados do veículo
    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable
            @NotNull
            @Positive
            Long vehicleId,
            @Valid
            @RequestBody
            VehiclePutRequest vehiclePutRequest
    ) {
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, vehiclePutRequest));
    }
}
