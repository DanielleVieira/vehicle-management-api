package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.mapper.ClientMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private ClientRepository repository;
    @Mock
    private ClientMapper mapper;
    @InjectMocks
    private ClientService service;

    private ClientCreateRequest clientCreateReq;
    private ClientPutRequest clientPutReq;
    private Client client;
    private Client updatedClient;
    private Client savedClient;
    private Client savedOtherClient;
    private ClientResponse clientRes;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        clientCreateReq = new ClientCreateRequest(
                "David",
                "david@gmail.com",
                "989.641.928-00",
                LocalDate.of(1991, 11, 30)
        );
        clientPutReq = new ClientPutRequest(
                "David Miranda Santos",
                "david@gmail.com",
                "630.997.389-48",
                LocalDate.of(1991, 11, 30)
        );
        client = new Client(
                "DAVID",
                "david@gmail.com",
                "98964192800",
                LocalDate.of(1991, 11, 30)
        );
        savedClient = new Client(
                "DAVID",
                "david@gmail.com",
                "98964192800",
                LocalDate.of(1991, 11, 30)
        );
        setProperty("id", 1L, savedClient);
        updatedClient = savedClient.updateClient(
                clientPutReq.name(),
                clientPutReq.email(),
                clientPutReq.cpf(),
                clientPutReq.birthDate());
        savedOtherClient = new Client(
                "RUAN",
                "ruan@gmail.com",
                "98964192800",
                LocalDate.of(1991, 11, 30)
        );
        setProperty("id", 2L, savedOtherClient);
        clientRes = new ClientResponse(
                1L,
                "DAVID",
                "david@gmail.com",
                "98964192800",
                LocalDate.of(1991, 11, 30)
        );
    }

    private void setProperty(String property, Object value, Client client) {
        try {
            Field field = Client.class.getDeclaredField(property);
            field.setAccessible(true);
            field.set(client, value); // Reflection pode ser usado para setar o id de cliente
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createClient_CreateAndSaveClient_Success_Test() {
        when(mapper.toEntity(clientCreateReq)).thenReturn(client);
        when(repository.findByCpf(client.getCpf())).thenReturn(Optional.empty());
        when(repository.save(client)).thenReturn(savedClient);
        when(mapper.toResponse(savedClient)).thenReturn(clientRes);

        var dbClient = service.createClient(clientCreateReq);

        assertEquals(clientRes, dbClient);
        verify(mapper).toEntity(clientCreateReq);
        verify(mapper).toResponse(savedClient);
        verify(repository).findByCpf(client.getCpf());
        verify(repository).save(client);
    }

    @Test
    public void createClient_DuplicatedCpf_ThrowException_Test() throws NoSuchFieldException, IllegalAccessException {
        when(mapper.toEntity(clientCreateReq)).thenReturn(client);
        when(repository.findByCpf(client.getCpf())).thenReturn(Optional.of(savedOtherClient));

        var exception = assertThrows(DuplicateResourceException.class, () -> service.createClient(clientCreateReq));
        assertEquals("CPF already registered", exception.getMessage());
        verify(mapper).toEntity(clientCreateReq);
        verify(repository).findByCpf(client.getCpf());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    public void updateClient_UpdateAndSaveClient_Success_Test() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedClient));
        // atualizando um client sem alterar cpf => mesmo id e mesmo cpf
        when(repository.findByCpf(updatedClient.getCpf())).thenReturn(Optional.of(savedClient));
        when(repository.save(updatedClient)).thenReturn(updatedClient);
        var clientRes = new ClientResponse(
                1L,
                updatedClient.getName(),
                updatedClient.getEmail(),
                updatedClient.getCpf(),
                updatedClient.getBirthDate()
        );
        when(mapper.toResponse(updatedClient)).thenReturn(clientRes);

        var response = service.updateClient(1L, clientPutReq);

        assertEquals(clientRes, response);
        verify(repository).findById(1L);
        verify(repository).findByCpf(updatedClient.getCpf());
        verify(repository).save(updatedClient);
        verify(mapper).toResponse(updatedClient);
    }

    @Test
    public void updateClient_ClientNotFound_ThrowException_Test() throws NoSuchFieldException, IllegalAccessException {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.updateClient(3L, clientPutReq));

        assertEquals("Client with id 3 not found", exception.getMessage());
        verify(repository).findById(3L);
        verify(repository, never()).findByCpf(any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    public void updateClient_DuplicatedCpf_ThrowException_Test() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedClient));
        setProperty("cpf", updatedClient.getCpf(), savedOtherClient);
        // atualizando um client com cpf já registrado por outro => mesmo cpf e id diferentes
        when(repository.findByCpf(updatedClient.getCpf())).thenReturn(Optional.of(savedOtherClient));

        var exception = assertThrows(DuplicateResourceException.class, () -> service.updateClient(1L, clientPutReq));

        assertEquals("CPF already registered", exception.getMessage());
        verify(repository).findById(1L);
        verify(repository).findByCpf(updatedClient.getCpf());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    public void deleteClient_DeleteClient_Success_Test() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deleteClient(1L);
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    public void deleteClient_ClientNotFound_ThrowException_Test() {
        when(repository.existsById(5L)).thenReturn(false);

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteClient(5L));

        assertEquals("Client with id 5 not found", exception.getMessage());
        verify(repository).existsById(5L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    public void getClient_GetClient_Success_Test() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(mapper.toResponse(savedClient)).thenReturn(clientRes);

        var response = service.getClient(1L);

        assertEquals(clientRes, response);
        verify(repository).findById(1L);
        verify(mapper).toResponse(savedClient);
    }

    @Test
    public void getClient_ClientNotFound_ThrowException_Test() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.getClient(5L));

        assertEquals("Client with id 5 not found", exception.getMessage());
        verify(repository).findById(5L);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    public void getAllClients_GetAllClients_Success_Test() {
        setProperty("cpf", "63099738948", savedOtherClient);
        var clientsList = List.of(savedClient, savedOtherClient);
        var responseList = List.of(
                clientRes,
                new ClientResponse(
                        savedOtherClient.getId(),
                        savedOtherClient.getName(),
                        savedOtherClient.getEmail(),
                        savedOtherClient.getCpf(),
                        savedOtherClient.getBirthDate()
                )
        );
        when(repository.findAll()).thenReturn(clientsList);
        when(mapper.toResponseList(clientsList)).thenReturn(responseList);

        var response = service.getAllClients();

        assertEquals(responseList, response);
        verify(repository).findAll();
        verify(mapper).toResponseList(clientsList);
    }

    @Test
    public void getAllClients_GetAllClients_EmptyList_Test() {
        when(repository.findAll()).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        var response = service.getAllClients();

        assertEquals(List.of(), response);
        verify(repository).findAll();
        verify(mapper).toResponseList(List.of());
    }
}
