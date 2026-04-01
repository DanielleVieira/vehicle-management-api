package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.GlobalExceptionHandler;
import com.github.daniellevieira.vehiclemanagementapi.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(GlobalExceptionHandler.class)
public class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ClientService service;
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // pra conseguir converter LocalDate
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pra não converter a data como array entre []

    private ClientCreateRequest clientCreateRequest;
    private ClientResponse clientResponse;

    @BeforeEach
    public void setup() {
        clientCreateRequest = new ClientCreateRequest(
                "Maxi",
                "maxi@gmail.com",
                "101.872.365-00",
                LocalDate.of(2025, 11, 7)
        );
        clientResponse = new ClientResponse(
                1L,
                "MAXI",
                "maxi@gmail.com",
                "10187236500",
                LocalDate.of(2025, 11, 7)
        );
    }

    @Test
    public void createClient_PostClient_Created_Test() throws Exception {
        when(service.createClient(clientCreateRequest)).thenReturn(clientResponse);

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
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        startsWith("email"),
                        startsWith("name"),
                        startsWith("cpf"),
                        startsWith("birthDate")
                )));
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


}
