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
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // TODO handle de id inválido ou nulo
    @DeleteMapping("/{id}")
    public ResponseEntity deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // TODO handle de id inválido ou nulo, e parâmetros incorretos ou usuário inexistente ou cpf repetido;
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @RequestBody ClientPutRequest clientPutRequest) {
        return ResponseEntity.ok(clientService.updateClient(id, clientPutRequest));
    }

}
