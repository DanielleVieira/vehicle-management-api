package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.mapper.ClientMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
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
        var newClient = saveClient(clientMapper.toEntity(clientCreateRequest));
        return clientMapper.toResponse(newClient);
    }

    public ClientResponse getClient(Long id) {
        var client = clientRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " not found"));
        return clientMapper.toResponse(client);
    }

    public List<ClientResponse> getAllClients() {
        var clients = clientRepository.findAll();
        return clientMapper.toResponseList(clients);
    }

    public void deleteClient(Long id) {
        var exists = clientRepository.existsById(id);
        if (exists) {
            clientRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Client with id " + id + " not found");
        }
    }

    public ClientResponse updateClient(Long id, ClientPutRequest clientReq) {
        var client = clientRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " not found"));
        client.updateClient(
                clientReq.name(),
                clientReq.email(),
                clientReq.cpf(),
                clientReq.birthDate()
        );
        return clientMapper.toResponse(saveClient(client));
    }

    private Client saveClient(Client client) {
        var savedClient =  clientRepository.findByCpf(client.getCpf());
        if(savedClient.isPresent() && !client.equals(savedClient.get())) {
            throw new DuplicateResourceException("CPF already registered");
        } else {
            return clientRepository.save(client);
        }
    }
}
