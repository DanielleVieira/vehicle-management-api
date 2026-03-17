package com.github.daniellevieira.vehiclemanagementapi.service;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.factory.VehicleFactory;
import com.github.daniellevieira.vehiclemanagementapi.mapper.VehicleMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import com.github.daniellevieira.vehiclemanagementapi.repository.ClientRepository;
import com.github.daniellevieira.vehiclemanagementapi.repository.VehicleRepository;
import org.springframework.stereotype.Service;

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
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }
}
