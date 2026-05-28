package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.GlobalExceptionHandler;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.security.auth.JwtAuthenticationFilter;
import com.github.daniellevieira.vehiclemanagementapi.security.auth.JwtService;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ClientService service;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // pra conseguir converter LocalDate
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pra não converter a data como array entre []

    private ClientCreateRequest clientCreateRequest;
    private ClientResponse createResponse;
    private ClientPutRequest clientPutRequest;
    private ClientResponse updateResponse;

    @BeforeEach
    public void setup() {
        clientCreateRequest = new ClientCreateRequest(
                "Maxi",
                "maxi@gmail.com",
                "101.872.365-00",
                LocalDate.of(2025, 11, 7)
        );
        clientPutRequest = new ClientPutRequest(
                "Maxi",
                "maxi@gmail.com",
                "338.941.000-79",
                LocalDate.of(2025, 11, 7)
        );
        createResponse = new ClientResponse(
                1L,
                "MAXI",
                "maxi@gmail.com",
                "10187236500",
                LocalDate.of(2025, 11, 7)
        );
        updateResponse = new ClientResponse(
                1L,
                "MAXI",
                "maxi@gmail.com",
                "33894100079",
                LocalDate.of(2025, 11, 7)
        );
    }

    @Test
    public void createClient_PostClient_Created_Test() throws Exception {
        when(service.createClient(clientCreateRequest)).thenReturn(createResponse);

        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/clients/1")))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("10187236500"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"));

    }

    @Test
    public void createClient_NullClient_BadRequest_Test() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Required request body is missing")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void createClient_NullParameters_BadRequest_Test() throws Exception {
        var json = """
                {
                }
                """;

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(4)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("email"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("name"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("cpf"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("birthDate"))));
    }

    @Test
    public void createClient_InvalidParameters_BadRequest_Test() throws Exception {
        var clientReq = new ClientCreateRequest(
                "st",
                "email.com",
                "123456",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("email"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("name"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("cpf"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("birthDate"))));
    }

    @Test
    public void createClient_DuplicatedCpf_Conflict_Test() throws Exception {
        when(service.createClient(clientCreateRequest)).thenThrow(new DuplicateResourceException("CPF already registered"));

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CPF already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    public void getClient_GetClient_Ok_Test() throws Exception {
        when(service.getClient(1L)).thenReturn(createResponse);

        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("10187236500"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void getClient_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(get("/api/v1/clients/a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/a"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void getClient_ClientNotFound_NotFound_Test() throws Exception {
        when(service.getClient(2L)).thenThrow(new ResourceNotFoundException("Client with id 2 not found"));

        mockMvc.perform(get("/api/v1/clients/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void getAllClients_GetClients_Ok_Test() throws Exception {
        Pageable page = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        when(service.getAllClients(page)).thenReturn(new PageImpl<>(List.of(createResponse, createResponse, createResponse)));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].cpf").value("10187236500"))
                .andExpect(jsonPath("$.content[0].birthDate").value("2025-11-07"))
                .andExpect(jsonPath("$.content[0].name").value("MAXI"))
                .andExpect(jsonPath("$.content[0].email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    public void getAllClients_EmptyList_Ok_Test() throws Exception {
        Pageable page = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        when(service.getAllClients(page)).thenReturn(new PageImpl<>(List.of()));


        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    public void deleteClient_DeleteClient_Ok_Test() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteClient_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/a"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void deleteClient_ClientNotFound_NotFound_Test() throws Exception {
        doThrow(new ResourceNotFoundException("Client with id 2 not found")).when(service).deleteClient(2L);

        mockMvc.perform(delete("/api/v1/clients/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void updateClient_UpdateClient_Ok_Test() throws Exception {
        when(service.updateClient(1L, clientPutRequest)).thenReturn(updateResponse);

        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("33894100079"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"));

    }

    @Test
    public void updateClient_NullClient_BadRequest_Test() throws Exception {
        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Required request body is missing")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void updateClient_NullParameters_BadRequest_Test() throws Exception {
        var json = """
                {
                }
                """;

        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(4)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("email"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("name"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("cpf"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("birthDate"))));
    }

    @Test
    public void updateClient_InvalidParameters_BadRequest_Test() throws Exception {
        var clientReq = new ClientPutRequest(
                "st",
                "email.com",
                "123456",
                LocalDate.now()
        );

        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        startsWith("email"),
                        startsWith("name"),
                        startsWith("cpf"),
                        startsWith("birthDate")
                )));
    }

    @Test
    public void updateClient_DuplicatedCpf_Conflict_Test() throws Exception {
        when(service.updateClient(1L, clientPutRequest)).thenThrow(new DuplicateResourceException("CPF already registered"));

        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CPF already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    public void updateClient_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(put("/api/v1/clients/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/-1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void updateClient_ClientNotFound_NotFound_Test() throws Exception {
        when(service.updateClient(2L, clientPutRequest)).thenThrow(new ResourceNotFoundException("Client with id 2 not found"));

        mockMvc.perform(put("/api/v1/clients/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }
}
