package com.github.daniellevieira.vehiclemanagementapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.daniellevieira.vehiclemanagementapi.dto.*;
import com.github.daniellevieira.vehiclemanagementapi.exception.GlobalExceptionHandler;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // para dasativar filtros do spring Security
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
public class VehicleIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ClientRepository clientRepository;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // pra conseguir converter LocalDate
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pra não converter a data como array entre []

    private VehiclePutRequest vehiclePutRequest;
    private ClientCreateRequest clientCreateRequest;
    private Long ownerId;

    @BeforeEach
    public void setup() {
        clientCreateRequest = new ClientCreateRequest(
                "Davi",
                "davi@gmail.com",
                "527.186.625-49",
                LocalDate.of(1991, 12, 19)
        );

        vehiclePutRequest = new VehiclePutRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "DEF-1234"
        );
    }

    private VehicleResponse getVehicleResponse(MvcResult result) throws Exception {
        var json = result.getResponse().getContentAsString();
        return mapper.readValue(json, VehicleResponse.class);
    }

    private Long postVehicleAndReturnId(VehicleCreateRequest vehicleCreateRequest) throws Exception {
        var httpResponse = mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(vehicleCreateRequest))).andReturn();
        return getVehicleResponse(httpResponse).id();
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
    public void createVehicle_PostVehicle_Created_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );

        var httpResponse = mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehicleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/vehicles/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.year").value("2025"))
                .andExpect(jsonPath("$.owner.id").value(ownerId))
                .andReturn();

        var response = getVehicleResponse(httpResponse);

        assertTrue(response.id() > 0);
        assertTrue(httpResponse.getResponse().getHeader("Location").contains("api/v1/vehicles/"+response.id()));

    }

    @Test
    public void createVehicle_PostVehicleWithoutTransactional_Created_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );

        var httpResponse = mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehicleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/vehicles/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.year").value("2025"))
                .andExpect(jsonPath("$.owner.id").value(ownerId))
                .andReturn();

        var response = getVehicleResponse(httpResponse);

        assertTrue(response.id() > 0);
        assertTrue(httpResponse.getResponse().getHeader("Location").contains("api/v1/vehicles/"+response.id()));

        vehicleRepository.deleteAll();
        clientRepository.deleteAll();
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
    @Transactional
    public void createVehicle_DuplicatedLicensePlate_Conflict_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        postVehicleAndReturnId(vehicleCreateRequest);

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
                LocalDate.now().getYear() + 1,
                "ABC-1234",
                1L
        );

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
    @Transactional
    public void getVehicle_GetVehicle_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles/" + vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.owner.id").value(ownerId));
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
    @Transactional
    public void getAllVehicles_GetVehicles_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(vehicleId))
                .andExpect(jsonPath("$.content[0].licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.content[0].year").value(2025))
                .andExpect(jsonPath("$.content[0].make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.content[0].model").value("T-CROSS"))
                .andExpect(jsonPath("$.content[0].owner.id").value(ownerId))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void getAllClients_EmptyList_Ok_Test() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Transactional
    public void getAllVehicles_WithOwnerId_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(vehicleId))
                .andExpect(jsonPath("$.content[0].owner.id").value(ownerId))
                .andExpect(jsonPath("$.content[0].licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Transactional
    public void getAllVehicles_WithOwnerId_EmptyList_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
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
    @Transactional
    public void getAllVehicles_ValidParameters_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles")
                                .param("ownerId", ownerId.toString())
                                .param("page", "0")
                                .param("size", "11")
                                .param("sort", "model,ASC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(vehicleId))
                .andExpect(jsonPath("$.content[0].licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.content[0].year").value(2025))
                .andExpect(jsonPath("$.content[0].make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.content[0].model").value("T-CROSS"))
                .andExpect(jsonPath("$.content[0].owner.id").value(ownerId))
                .andExpect(jsonPath("$.size").value(11))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Transactional
    public void getAllVehicles_InvalidParameter_BadRequest_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);

        mockMvc.perform(get("/api/v1/vehicles")
                                .param("ownerId", ownerId.toString())
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "mode,ASC")
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasItem(containsString("mode"))));
    }

    @Test
    @Transactional
    public void deleteVehicle_DeleteVehicle_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(delete("/api/v1/vehicles/" + vehicleId))
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
    @Transactional
    public void updateVehicle_UpdateVehicle_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("DEF1234"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.owner").exists());

    }

    @Test
    public void updateVehicle_UpdateVehicleWithoutTransactional_Ok_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.make").value("VOLKSWAGEN"))
                .andExpect(jsonPath("$.model").value("T-CROSS"))
                .andExpect(jsonPath("$.licensePlate").value("DEF1234"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.owner").exists());

        vehicleRepository.deleteAll();
        clientRepository.deleteAll();
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
    @Transactional
    public void updateVehicle_DuplicatedLicensePlate_Conflict_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleReq = new VehicleCreateRequest(
                "Chevrolet",
                "Tracker",
                2025,
                "DEF-1234",
                ownerId
        );
        var otherVehicleId = postVehicleAndReturnId(vehicleReq);
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);

        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vehiclePutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vehicle license plate already registered"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/" + vehicleId))
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
    @Transactional
    public void updateVehicle_VehicleYearInFuture_UnprocessableContent_Test() throws Exception {
        var ownerId = postClientAndReturnId(clientCreateRequest);
        var vehicleCreateRequest = new VehicleCreateRequest(
                "Volkswagen",
                "T-Cross",
                2025,
                "ABC-1234",
                ownerId
        );
        var vehicleId = postVehicleAndReturnId(vehicleCreateRequest);
        var invalidPutRequest = new VehiclePutRequest(
                "Volkswagen",
                "T-Cross",
                LocalDate.now().getYear() + 1,
                "ABC-1234"
        );

        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidPutRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("The vehicle's year must be a present or past value"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/" + vehicleId))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(0)));
    }
}
