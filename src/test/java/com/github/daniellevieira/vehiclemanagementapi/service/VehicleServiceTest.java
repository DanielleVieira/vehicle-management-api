package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehiclePutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.BusinessException;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.factory.VehicleFactory;
import com.github.daniellevieira.vehiclemanagementapi.mapper.VehicleMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private VehicleMapper vehicleMapper;
    @Mock
    private VehicleFactory vehicleFactory;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private VehicleService service;

    private VehicleCreateRequest vehicleCreateReq;
    private VehiclePutRequest vehiclePutReq;
    private Client owner;
    private ClientResponse ownerRes;
    private Vehicle vehicle;
    private Vehicle savedVehicle;
    private Vehicle updatedVehicle;
    private Vehicle otherVehicle;
    private VehicleResponse vehicleRes;

    @BeforeEach
    public void setup() {
        owner = new Client(
                "David",
                "david@gmail.com",
                "989.641.928-00",
                LocalDate.of(1991, 11, 30)
        );
        setProperty(Client.class, owner, "id", 1L);

        ownerRes = new ClientResponse(
                1L,
                "DAVID",
                "david@gmail.com",
                "98964192800",
                LocalDate.of(1991, 11, 30)
        );

        vehicleCreateReq = new VehicleCreateRequest(
                "Fiat",
                "Uno",
                2012,
                "ABC-1234",
                1L
        );
        vehiclePutReq = new VehiclePutRequest(
                "Volkswagen",
                "Gol",
                2015,
                "ABC-1234"
        );

        vehicle = new Vehicle(
                vehicleCreateReq.make(),
                vehicleCreateReq.model(),
                vehicleCreateReq.year(),
                vehicleCreateReq.licensePlate(),
                owner
        );

        savedVehicle = new Vehicle(
                vehicleCreateReq.make(),
                vehicleCreateReq.model(),
                vehicleCreateReq.year(),
                vehicleCreateReq.licensePlate(),
                owner
        );
        setProperty(Vehicle.class, savedVehicle, "id", 1L);

        updatedVehicle = savedVehicle.updateVehicle(
                vehiclePutReq.make(),
                vehiclePutReq.model(),
                vehiclePutReq.year(),
                vehiclePutReq.licensePlate()
        );

        otherVehicle = new Vehicle(
                "Ford",
                "Ka",
                2018,
                "XYZ-9999",
                owner
        );
        setProperty(Vehicle.class, otherVehicle, "id", 2L);

        vehicleRes = new VehicleResponse(
                1L,
                "FIAT",
                "UNO",
                2012,
                "ABC1234",
                ownerRes
        );
    }

    private void setProperty(Class<?> type, Object target, String property, Object value) {
        try {
            Field field = type.getDeclaredField(property);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createVehicle_CreateAndSaveVehicle_Success_Test() {
        when(clientRepository.findById(vehicleCreateReq.ownerId())).thenReturn(Optional.of(owner));
        when(vehicleFactory.create(vehicleCreateReq, owner)).thenReturn(vehicle);
        when(vehicleRepository.findByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.empty());
        when(vehicleRepository.save(vehicle)).thenReturn(savedVehicle);
        when(vehicleMapper.toResponse(savedVehicle)).thenReturn(vehicleRes);

        var response = service.createVehicle(vehicleCreateReq);

        assertEquals(vehicleRes, response);
        verify(clientRepository).findById(vehicleCreateReq.ownerId());
        verify(vehicleFactory).create(vehicleCreateReq, owner);
        verify(vehicleRepository).findByLicensePlate(vehicle.getLicensePlate());
        verify(vehicleRepository).save(vehicle);
        verify(vehicleMapper).toResponse(savedVehicle);
    }

    @Test
    public void createVehicle_OwnerNotFound_ThrowException_Test() {
        when(clientRepository.findById(vehicleCreateReq.ownerId())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.createVehicle(vehicleCreateReq));

        assertEquals("Owner with id 1 not found", exception.getMessage());
        verify(clientRepository).findById(vehicleCreateReq.ownerId());
        verify(vehicleFactory, never()).create(any(), any());
        verify(vehicleRepository, never()).findByLicensePlate(any());
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void createVehicle_DuplicatedLicensePlate_ThrowException_Test() {
        when(clientRepository.findById(vehicleCreateReq.ownerId())).thenReturn(Optional.of(owner));
        when(vehicleFactory.create(vehicleCreateReq, owner)).thenReturn(vehicle);
        when(vehicleRepository.findByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.of(otherVehicle));

        var exception = assertThrows(DuplicateResourceException.class, () -> service.createVehicle(vehicleCreateReq));

        assertEquals("Vehicle license plate already registered", exception.getMessage());
        verify(clientRepository).findById(vehicleCreateReq.ownerId());
        verify(vehicleFactory).create(vehicleCreateReq, owner);
        verify(vehicleRepository).findByLicensePlate(vehicle.getLicensePlate());
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void createVehicle_FutureYear_ThrowException_Test() {
        var invalidRequest = new VehicleCreateRequest(
                "Fiat",
                "Uno",
                LocalDate.now().getYear() + 1,
                "ABC-1234",
                1L
        );

        var exception = assertThrows(BusinessException.class, () -> service.createVehicle(invalidRequest));

        assertEquals("The vehicle's year must be a present or past value", exception.getMessage());
        verify(clientRepository, never()).findById(any());
        verify(vehicleFactory, never()).create(any(), any());
        verify(vehicleRepository, never()).findByLicensePlate(any());
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void updateVehicle_UpdateAndSaveVehicle_Success_Test() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));
        when(vehicleRepository.findByLicensePlate(updatedVehicle.getLicensePlate())).thenReturn(Optional.of(savedVehicle));
        when(vehicleRepository.save(updatedVehicle)).thenReturn(updatedVehicle);

        var updatedResponse = new VehicleResponse(
                updatedVehicle.getId(),
                updatedVehicle.getMake(),
                updatedVehicle.getModel(),
                updatedVehicle.getYear(),
                updatedVehicle.getLicensePlate(),
                ownerRes
        );
        when(vehicleMapper.toResponse(updatedVehicle)).thenReturn(updatedResponse);

        var response = service.updateVehicle(1L, vehiclePutReq);

        assertEquals(updatedResponse, response);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).findByLicensePlate(updatedVehicle.getLicensePlate());
        verify(vehicleRepository).save(updatedVehicle);
        verify(vehicleMapper).toResponse(updatedVehicle);
    }

    @Test
    public void updateVehicle_VehicleNotFound_ThrowException_Test() {
        when(vehicleRepository.findById(3L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.updateVehicle(3L, vehiclePutReq));

        assertEquals("Vehicle with id 3 not found", exception.getMessage());
        verify(vehicleRepository).findById(3L);
        verify(vehicleRepository, never()).findByLicensePlate(any());
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void updateVehicle_DuplicatedLicensePlate_ThrowException_Test() {
        var duplicatedPlateRequest = new VehiclePutRequest(
                "Volkswagen",
                "Gol",
                2015,
                "XYZ-9999"
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));
        when(vehicleRepository.findByLicensePlate("XYZ9999")).thenReturn(Optional.of(otherVehicle));

        var exception = assertThrows(DuplicateResourceException.class, () -> service.updateVehicle(1L, duplicatedPlateRequest));

        assertEquals("Vehicle license plate already registered", exception.getMessage());
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).findByLicensePlate("XYZ9999");
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void updateVehicle_FutureYear_ThrowException_Test() {
        var invalidRequest = new VehiclePutRequest(
                "Volkswagen",
                "Gol",
                LocalDate.now().getYear() + 1,
                "ABC-1234"
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));

        var exception = assertThrows(BusinessException.class, () -> service.updateVehicle(1L, invalidRequest));

        assertEquals("The vehicle's year must be a present or past value", exception.getMessage());
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).findByLicensePlate(any());
        verify(vehicleRepository, never()).save(any());
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void deleteVehicle_DeleteVehicle_Success_Test() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        service.deleteVehicle(1L);

        verify(vehicleRepository).existsById(1L);
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    public void deleteVehicle_VehicleNotFound_ThrowException_Test() {
        when(vehicleRepository.existsById(5L)).thenReturn(false);

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteVehicle(5L));

        assertEquals("Vehicle with id 5 not found", exception.getMessage());
        verify(vehicleRepository).existsById(5L);
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    public void getVehicle_GetVehicle_Success_Test() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));
        when(vehicleMapper.toResponse(savedVehicle)).thenReturn(vehicleRes);

        var response = service.getVehicle(1L);

        assertEquals(vehicleRes, response);
        verify(vehicleRepository).findById(1L);
        verify(vehicleMapper).toResponse(savedVehicle);
    }

    @Test
    public void getVehicle_VehicleNotFound_ThrowException_Test() {
        when(vehicleRepository.findById(5L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.getVehicle(5L));

        assertEquals("Vehicle with id 5 not found", exception.getMessage());
        verify(vehicleRepository).findById(5L);
        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    public void getAllVehicles_GetAllVehicles_Success_Test() {
        var vehiclesList = List.of(savedVehicle, otherVehicle);
        var responseList = List.of(
                vehicleRes,
                new VehicleResponse(
                        2L,
                        "FORD",
                        "KA",
                        2018,
                        "XYZ9999",
                        ownerRes
                )
        );
        when(vehicleRepository.findAll()).thenReturn(vehiclesList);
        when(vehicleMapper.toResponseList(vehiclesList)).thenReturn(responseList);

        var response = service.getAllVehicles();

        assertEquals(responseList, response);
        verify(vehicleRepository).findAll();
        verify(vehicleMapper).toResponseList(vehiclesList);
    }

    @Test
    public void getAllVehicles_GetAllVehicles_EmptyList_Test() {
        when(vehicleRepository.findAll()).thenReturn(List.of());
        when(vehicleMapper.toResponseList(List.of())).thenReturn(List.of());

        var response = service.getAllVehicles();

        assertEquals(List.of(), response);
        verify(vehicleRepository).findAll();
        verify(vehicleMapper).toResponseList(List.of());
    }

    @Test
    public void getVehiclesByOwnerId_GetVehicles_Success_Test() {
        var vehiclesList = List.of(savedVehicle, otherVehicle);
        var responseList = List.of(
                vehicleRes,
                new VehicleResponse(
                        2L,
                        "FORD",
                        "KA",
                        2018,
                        "XYZ9999",
                        ownerRes
                )
        );
        when(clientRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(vehicleRepository.findAllByOwner(owner)).thenReturn(vehiclesList);
        when(vehicleMapper.toResponseList(vehiclesList)).thenReturn(responseList);

        var response = service.getVehiclesByOwnerId(1L);

        assertEquals(responseList, response);
        verify(clientRepository).findById(1L);
        verify(vehicleRepository).findAllByOwner(owner);
        verify(vehicleMapper).toResponseList(vehiclesList);
    }

    @Test
    public void getVehiclesByOwnerId_OwnerNotFound_ThrowException_Test() {
        when(clientRepository.findById(10L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.getVehiclesByOwnerId(10L));

        assertEquals("Owner with id 10 not found", exception.getMessage());
        verify(clientRepository).findById(10L);
        verify(vehicleRepository, never()).findAllByOwner(any());
        verify(vehicleMapper, never()).toResponseList(any());
    }
}
