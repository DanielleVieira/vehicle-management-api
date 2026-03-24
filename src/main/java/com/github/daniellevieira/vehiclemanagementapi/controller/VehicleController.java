package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehiclePutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    // TODO handle para quando o vehicle ou campos vem nulo ou incorretos e quando clientId é incorreto e placa repetida no banco
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

    // TODO handle pro caso de ownerId inválido, ou owner não encontrado
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false)
            @Positive
            Long ownerId
    ) {
        if (ownerId != null) {
            return ResponseEntity.ok(vehicleService.getVehiclesByOwnerId(ownerId));
        }
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // TODO handle de veículo não encontrado, id inválido ou nulo
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicle(
            @PathVariable
            @NotNull
            @Positive
            Long vehicleId
    ) {
        return ResponseEntity.ok(vehicleService.getVehicle(vehicleId));
    }

    // TODO handle de id nulo ou inválido
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

    // TODO handle id de veículo inválido ou nulo, parâmentros inválidos ou nulos, veículo não encontrado, e placa repetida no banco
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
