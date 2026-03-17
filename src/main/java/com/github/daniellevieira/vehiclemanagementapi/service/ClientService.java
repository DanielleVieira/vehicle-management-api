package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.mapper.ClientMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    // TODO validar cpf repetido
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

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    public ClientResponse updateClient(Long id, ClientPutRequest clientReq) {
        var client = clientRepository.findById(id).orElseThrow();
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
        if(savedClient.isPresent() && savedClient.get().getId() != client.getId()) {
           // TODO criar a execeção
            return null;
        } else {
            return clientRepository.save(client);
        }
    }
}
