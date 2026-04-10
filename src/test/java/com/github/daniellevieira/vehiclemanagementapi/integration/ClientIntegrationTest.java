package com.github.daniellevieira.vehiclemanagementapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.daniellevieira.vehiclemanagementapi.controller.ClientController;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientPutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.GlobalExceptionHandler;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
public class ClientIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientRepository repository;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // pra conseguir converter LocalDate
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pra não converter a data como array entre []

    private ClientCreateRequest clientCreateRequest;
    private ClientPutRequest clientPutRequest;

    @BeforeEach
    public void setup() throws Exception {
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
    }

    private ClientResponse getClientResponse(MvcResult result) throws Exception {
        var json = result.getResponse().getContentAsString();
        return mapper.readValue(json, ClientResponse.class);
    }

    private Long postClientAndReturnId(ClientCreateRequest clientCreateRequest) throws Exception {
        var httpResponse = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(clientCreateRequest))).andReturn();
        return getClientResponse(httpResponse).id();
    }

    @Test
    @Transactional
    public void createClient_PostClient_Created_Test() throws Exception {
        var httpResponse = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("10187236500"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"))
                .andReturn();

        var response = getClientResponse(httpResponse);

        assertTrue(response.id() > 0);
        assertTrue(httpResponse.getResponse().getHeader("Location").contains("api/v1/clients/"+response.id()));
    }

    // teste sem o transactional pra testar com o commit do banco
    @Test
    public void createClient_PostClientWithoutTransactional_Created_Test() throws Exception {
        var httpResponse = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("10187236500"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"))
                .andReturn();

        var response = getClientResponse(httpResponse);

        assertTrue(response.id() > 0);
        assertTrue(httpResponse.getResponse().getHeader("Location").contains("api/v1/clients/"+response.id()));
        repository.deleteAll();
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
    @Transactional
    public void createClient_DuplicatedCpf_Conflict_Test() throws Exception {
        postClientAndReturnId(clientCreateRequest);
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
    @Transactional
    public void getClient_GetClient_Ok_Test() throws Exception {
        var clientId = postClientAndReturnId(clientCreateRequest);
        mockMvc.perform(get("/api/v1/clients/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("10187236500"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.id").value(clientId));
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
    @Transactional
    public void getAllClients_GetClients_Ok_Test() throws Exception {
        var clientId = postClientAndReturnId(clientCreateRequest);
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(clientId))
                .andExpect(jsonPath("$[0].cpf").value("10187236500"))
                .andExpect(jsonPath("$[0].birthDate").value("2025-11-07"))
                .andExpect(jsonPath("$[0].name").value("MAXI"))
                .andExpect(jsonPath("$[0].email").value("maxi@gmail.com"));
    }

    @Test
    public void getAllClients_EmptyList_Ok_Test() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void deleteClient_DeleteClient_Ok_Test() throws Exception {
        var clientId = postClientAndReturnId(clientCreateRequest);
        mockMvc.perform(delete("/api/v1/clients/" + clientId))
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
    @Transactional
    public void updateClient_UpdateClient_Ok_Test() throws Exception {
        var clientId = postClientAndReturnId(clientCreateRequest);
        mockMvc.perform(put("/api/v1/clients/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("33894100079"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"));

    }

    @Test
    public void updateClient_UpdateClientWithoutTransactional_Ok_Test() throws Exception {
        var clientId = postClientAndReturnId(clientCreateRequest);
        mockMvc.perform(put("/api/v1/clients/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.name").value("MAXI"))
                .andExpect(jsonPath("$.email").value("maxi@gmail.com"))
                .andExpect(jsonPath("$.cpf").value("33894100079"))
                .andExpect(jsonPath("$.birthDate").value("2025-11-07"));

        repository.deleteAll();
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
    @Transactional
    public void updateClient_DuplicatedCpf_Conflict_Test() throws Exception {
        var clientReq = new ClientCreateRequest(
                "Marilda",
                "marilda@gmail.com",
                "338.941.000-79",
                LocalDate.of(2025, 11, 7)
        );
        var otherClientId = postClientAndReturnId(clientReq);
        var clientId = postClientAndReturnId(clientCreateRequest);

        mockMvc.perform(put("/api/v1/clients/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientPutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CPF already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/clients/" + clientId))
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
