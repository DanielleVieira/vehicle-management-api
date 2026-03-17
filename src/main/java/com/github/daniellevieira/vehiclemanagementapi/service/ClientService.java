package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.mapper.ClientMapper;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    public ClientResponse createClient(ClientCreateRequest clientCreateRequest) {
        var newClient = clientRepository.save(clientMapper.toEntity(clientCreateRequest));
        return clientMapper.toResponse(newClient);
    }

    public ClientResponse getClient(Long id) {
        var client = clientRepository.findById(id).orElseThrow();
        return clientMapper.toResponse(client);
    }

    public List<ClientResponse> getAllClients() {
        var clients = clientRepository.findAll();
        return clientMapper.toResponseList(clients);
    }
}
