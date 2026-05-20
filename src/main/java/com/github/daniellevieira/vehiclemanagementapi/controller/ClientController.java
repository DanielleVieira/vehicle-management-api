package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
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
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;

import java.util.List;

@Validated
@RestController
@RequestMapping("api/v1/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid
            @RequestBody
            ClientCreateRequest clientCreateRequest
    ) {
        var clientRes = clientService.createClient(clientCreateRequest);
        var location = UriUtils.createLocationFromCurrentRequest(Long.toString(clientRes.id()));
        return ResponseEntity.created(location).body(clientRes);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(
            @PathVariable
            @NotNull
            @Positive(message = "must be greater than 0")
            Long clientId
    ) {
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    // TODO testes e handle para o caso de PropertyReferenceException do pageable
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            @ParameterObject
            Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.getAllClients(pageable));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity deleteClient(
            @PathVariable
            @NotNull
            @Positive(message = "must be greater than 0")
            Long clientId
    ) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable
            @NotNull
            @Positive(message = "must be greater than 0")
            Long clientId,
            @Valid
            @RequestBody
            ClientPutRequest clientPutRequest
    ) {
        return ResponseEntity.ok(clientService.updateClient(clientId, clientPutRequest));
    }

}
