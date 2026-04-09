package com.github.daniellevieira.vehiclemanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.daniellevieira.vehiclemanagementapi.dto.*;
import com.github.daniellevieira.vehiclemanagementapi.exception.BusinessException;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.GlobalExceptionHandler;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
public class VehicleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private VehicleService service;
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // pra conseguir converter LocalDate
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pra não converter a data como array entre []

    private VehicleCreateRequest vehicleCreateRequest;
    private VehicleResponse createResponse;
    private VehiclePutRequest vehiclePutRequest;
    private VehicleResponse updateResponse;
    private ClientResponse ownerResponse;

    @BeforeEach
    public void setup() {
        vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                1L
        );

        vehiclePutRequest = new VehiclePutRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "DEF-1234"
        );

        ownerResponse = new ClientResponse(
                1L,
                "DAVI",
                "davi@gmail.com",
                "52718662549",
                LocalDate.of(1991, 11, 15)
        );

        createResponse = new VehicleResponse(
                1L,
                "VOLKSWAGEN",
                "T-CROSS",
                2025,
                "ABC1234",
                ownerResponse
        );

        updateResponse = new VehicleResponse(
                1L,
                "VOLKSWAGEN",
                "T-CROSS",
                2025,
                "DEF1234",
                ownerResponse
        );
    }

    @Test
    public void createVehicle_PostVehicle_Created_Test() throws Exception {
        when(service.createVehicle(vehicleCreateRequest)).thenReturn(createResponse);

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(vehicleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/vehicles/1")))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.year").value("2025"))
                .andExpect(jsonPath("$.owner.id").value(1L));

    }

    @Test
    public void createVehicle_NullVehicle_BadRequest_Test() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Required request body is missing")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void createVehicle_NullParameters_BadRequest_Test() throws Exception {
        var json = """
                {
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(5)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("make"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("model"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("licensePlate"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("year"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("ownerId"))));
    }

    @Test
    public void createVehicle_InvalidParameters_BadRequest_Test() throws Exception {
        var clientReq = new VehicleCreateRequest(
                "Vo",
                "T-",
                1800,
                "ABC-12",
                -1L
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clientReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("make"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("model"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("licensePlate"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("ownerId"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("year"))));
    }

    @Test
    public void createVehicle_DuplicatedLicensePlate_Conflict_Test() throws Exception {
        when(service.createVehicle(vehicleCreateRequest)).thenThrow(new DuplicateResourceException("Vehicle license plate already registered"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehicleCreateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vehicle license plate already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    public void createVehicle_VehicleYearInFuture_UnprocessableContent_Test() throws Exception {
        var invalidCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2035,
                "ABC-1234",
                1L
        );

        when(service.createVehicle(invalidCreateRequest)).thenThrow(new BusinessException("The vehicle's year must be a present or past value"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidCreateRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("The vehicle's year must be a present or past value"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    public void getVehicle_GetVehicle_Ok_Test() throws Exception {
        when(service.getVehicle(1L)).thenReturn(createResponse);

        mockMvc.perform(get("/api/v1/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.owner.id").value(1L));
    }

    @Test
    public void getVehicle_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/a"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void getVehicle_VehicleNotFound_NotFound_Test() throws Exception {
        when(service.getVehicle(2L)).thenThrow(new ResourceNotFoundException("Vehicle with id 2 not found"));

        mockMvc.perform(get("/api/v1/vehicles/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void getAllVehicles_GetVehicles_Ok_Test() throws Exception {
        when(service.getAllVehicles()).thenReturn(Arrays.asList(createResponse, createResponse, createResponse));

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$[0].year").value(2025))
                .andExpect(jsonPath("$[0].make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$[0].model").value("T-CROSS"))
                .andExpect(jsonPath("$[0].owner.id").value(1L));;
    }

    @Test
    public void getAllClients_EmptyList_Ok_Test() throws Exception {
        when(service.getAllVehicles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getAllVehicles_WithOwnerId_Ok_Test() throws Exception {
        when(service.getVehiclesByOwnerId(1L)).thenReturn(Arrays.asList(createResponse, updateResponse));

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].owner.id").value(1L))
                .andExpect(jsonPath("$[1].licensePlate").value("DEF1234"));
    }

    @Test
    public void getAllVehicles_WithOwnerId_EmptyList_Ok_Test() throws Exception {
        when(service.getVehiclesByOwnerId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getAllVehicles_InvalidOwnerId_BadRequest_Test() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid parameter: Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasItem(containsString("ownerId"))));
    }

    @Test
    public void getAllVehicles_OwnerNotFound_NotFound_Test() throws Exception {
        when(service.getVehiclesByOwnerId(2L)).thenThrow(new ResourceNotFoundException("Owner with id 2 not found"));

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Owner with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void deleteVehicle_DeleteVehicle_Ok_Test() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteVehicle_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/a"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void deleteVehicle_VehicleNotFound_NotFound_Test() throws Exception {
        doThrow(new ResourceNotFoundException("Vehicle with id 2 not found")).when(service).deleteVehicle(2L);

        mockMvc.perform(delete("/api/v1/vehicles/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void updateVehicle_UpdateVehicle_Ok_Test() throws Exception {
        when(service.updateVehicle(1L, vehiclePutRequest)).thenReturn(updateResponse);

        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("DEF1234"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.owner").exists());

    }

    @Test
    public void updateVehicle_NullVehicle_BadRequest_Test() throws Exception {
        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Required request body is missing")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void updateVehicle_NullParameters_BadRequest_Test() throws Exception {
        var json = """
                {
                }
                """;

        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(4)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("make"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("model"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("licensePlate"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("year"))));
    }

    @Test
    public void updateVehicle_InvalidParameters_BadRequest_Test() throws Exception {
        var vehicleReq = new VehiclePutRequest(
                "Vo",
                "T-",
                1800,
                "DEF12344"
        );

        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehicleReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please check your request parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        startsWith("make"),
                        startsWith("model"),
                        startsWith("licensePlate"),
                        startsWith("year")
                )));
    }

    @Test
    public void updateVehicle_DuplicatedLicensePlate_Conflict_Test() throws Exception {
        when(service.updateVehicle(1L, vehiclePutRequest)).thenThrow(new DuplicateResourceException("Vehicle license plate already registered"));

        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vehicle license plate already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    public void updateVehicle_InvalidId_BadRequest_Test() throws Exception {
        mockMvc.perform(put("/api/v1/vehicles/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/-1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void updateVehicle_VehicleNotFound_NotFound_Test() throws Exception {
        when(service.updateVehicle(2L, vehiclePutRequest)).thenThrow(new ResourceNotFoundException("Vehicle with id 2 not found"));

        mockMvc.perform(put("/api/v1/vehicles/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle with id 2 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/2"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    public void updateVehicle_VehicleYearInFuture_UnprocessableContent_Test() throws Exception {
        var invalidPutRequest = new VehiclePutRequest(
                "Volkswagen",
                "T-Cross",
                2030,
                "ABC-1234"
        );

        when(service.updateVehicle(1L, invalidPutRequest)).thenThrow(new BusinessException("The vehicle's year must be a present or past value"));

        mockMvc.perform(put("/api/v1/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidPutRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("The vehicle's year must be a present or past value"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/1"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }
}
