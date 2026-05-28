package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehiclePutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.BusinessException;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.factory.VehicleFactory;
import com.github.daniellevieira.vehiclemanagementapi.mapper.VehicleMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleFactory vehicleFactory;
    private final ClientRepository clientRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            VehicleMapper vehicleMapper,
            VehicleFactory vehicleFactory,
            ClientRepository clientRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
        this.vehicleFactory = vehicleFactory;
        this.clientRepository = clientRepository;
    }

    public VehicleResponse createVehicle(VehicleCreateRequest vehicleCreateRequest) {
        validateVehicleYear(vehicleCreateRequest.year());
        var owner = clientRepository
                .findById(vehicleCreateRequest.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner with id " + vehicleCreateRequest.ownerId() + " not found"));
        var vehicle = vehicleFactory.create(vehicleCreateRequest, owner);
        return vehicleMapper.toResponse(saveVehicle(vehicle));
    }

    public Page<VehicleResponse> getAllVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(vehicleMapper::toResponse);
    }

    public VehicleResponse getVehicle(Long vehicleId) {
        var vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle with id " + vehicleId + " not found"));
        return vehicleMapper.toResponse(vehicle);
    }

    public void deleteVehicle(Long vehicleId) {
        var exists = vehicleRepository.existsById(vehicleId);
        if (exists) {
            vehicleRepository.deleteById(vehicleId);
        } else {
            throw new ResourceNotFoundException("Vehicle with id " + vehicleId + " not found");
        }
    }

    public VehicleResponse updateVehicle(Long vehicleId, VehiclePutRequest vehicleReq) {
        var vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle with id " + vehicleId + " not found"));
        validateVehicleYear(vehicleReq.year());
        vehicle = vehicle.updateVehicle(
                vehicleReq.make(),
                vehicleReq.model(),
                vehicleReq.year(),
                vehicleReq.licensePlate()
        );
        return vehicleMapper.toResponse(saveVehicle(vehicle));
    }

    public Page<VehicleResponse> getVehiclesByOwnerId(Long ownerId, Pageable pageable) {
        var owner = clientRepository
                .findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner with id " + ownerId + " not found"));
        var vehicles = vehicleRepository.findAllByOwner(owner, pageable);
        return vehicles.map(vehicleMapper::toResponse);
    }

    private Vehicle saveVehicle(Vehicle vehicle) {
        var savedVehicle =  vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
        if(savedVehicle.isPresent() && !vehicle.equals(savedVehicle.get())) {
            throw new DuplicateResourceException("Vehicle license plate already registered");
        } else {
            return vehicleRepository.save(vehicle);
        }
    }

    private void validateVehicleYear(int year) {
        if(year > LocalDate.now().getYear()) {
            throw new BusinessException("The vehicle's year must be a present or past value");
        }
    }
}
