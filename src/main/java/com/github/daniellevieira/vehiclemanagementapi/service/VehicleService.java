package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehiclePutRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.factory.VehicleFactory;
import com.github.daniellevieira.vehiclemanagementapi.mapper.VehicleMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        var owner = clientRepository
                .findById(vehicleCreateRequest.ownerId())
                .orElseThrow();
        var vehicle = vehicleFactory.create(vehicleCreateRequest, owner);
        return vehicleMapper.toResponse(saveVehicle(vehicle));
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleMapper.toResponseList(vehicleRepository.findAll());
    }

    public VehicleResponse getVehicle(Long vehicleId) {
        var vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        return vehicleMapper.toResponse(vehicle);
    }

    public void deleteVehicle(Long vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }

    public VehicleResponse updateVehicle(Long vehicleId, VehiclePutRequest vehicleReq) {
        var vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        vehicle.updateVehicle(
                vehicleReq.make(),
                vehicleReq.model(),
                vehicleReq.year(),
                vehicleReq.licensePlate()
        );
        return vehicleMapper.toResponse(saveVehicle(vehicle));
    }

    private Vehicle saveVehicle(Vehicle vehicle) {
        var savedVehicle =  vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
        if(savedVehicle.isPresent() && notHaveEqualsLicensePlate(vehicle, savedVehicle.get())) {
            // TODO criar a execeção
            return null;
        } else {
            return vehicleRepository.save(vehicle);
        }
    }

    private boolean notHaveEqualsLicensePlate(Vehicle vehicle, Vehicle savedVehicle) {
        return !savedVehicle.getLicensePlate().equals(vehicle.getLicensePlate());
    }
}
