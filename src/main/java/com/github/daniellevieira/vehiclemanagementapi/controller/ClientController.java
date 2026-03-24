package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    // TODO handle para quando o client ou campos vem nulo ou incorretos ou cpf repetido (exceções do código e do banco)
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

    // TODO handle de cliente não encontrado e id inválido ou nulo
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(
            @PathVariable
            @NotNull
            @Positive
            Long clientId
    ) {
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // TODO handle de id inválido ou nulo
    @DeleteMapping("/{clientId}")
    public ResponseEntity deleteClient(
            @PathVariable
            @NotNull
            @Positive
            Long clientId
    ) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    // TODO handle de id inválido ou nulo, e parâmetros incorretos ou usuário inexistente ou cpf repetido;
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable
            @NotNull
            @Positive
            Long clientId,
            @Valid
            @RequestBody
            ClientPutRequest clientPutRequest
    ) {
        return ResponseEntity.ok(clientService.updateClient(clientId, clientPutRequest));
    }

}
