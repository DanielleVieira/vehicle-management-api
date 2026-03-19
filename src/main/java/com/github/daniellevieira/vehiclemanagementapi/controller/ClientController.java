package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;

import java.util.List;

@RestController
@RequestMapping("api/v1/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // TODO handle para quando o client ou campos vem nulo ou incorretos ou cpf repetido (exceções do código e do banco)
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@RequestBody ClientCreateRequest clientCreateRequest) {
        var clientRes = clientService.createClient(clientCreateRequest);
        var location = UriUtils.createLocationFromCurrentRequest(Long.toString(clientRes.id()));
        return ResponseEntity.created(location).body(clientRes);
    }

    // TODO handle de cliente não encontrado e id inválido ou nulo
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // TODO handle de id inválido ou nulo
    @DeleteMapping("/{clientId}")
    public ResponseEntity deleteClient(@PathVariable Long clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    // TODO handle de id inválido ou nulo, e parâmetros incorretos ou usuário inexistente ou cpf repetido;
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long clientId, @RequestBody ClientPutRequest clientPutRequest) {
        return ResponseEntity.ok(clientService.updateClient(clientId, clientPutRequest));
    }

}
