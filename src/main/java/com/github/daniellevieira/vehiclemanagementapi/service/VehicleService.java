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

import java.time.LocalDate;
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

    // TODO verificar se o ano é menor ou igual ao atual
    public VehicleResponse createVehicle(VehicleCreateRequest vehicleCreateRequest) {
        validateVehicleYear(vehicleCreateRequest.year());
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

    // TODO acrescentar verificação de se o vehicle existe para retornar uma exceção caso não
    public void deleteVehicle(Long vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }

    public VehicleResponse updateVehicle(Long vehicleId, VehiclePutRequest vehicleReq) {
        validateVehicleYear(vehicleReq.year());
        var vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        vehicle.updateVehicle(
                vehicleReq.make(),
                vehicleReq.model(),
                vehicleReq.year(),
                vehicleReq.licensePlate()
        );
        return vehicleMapper.toResponse(saveVehicle(vehicle));
    }

    public List<VehicleResponse> getVehiclesByOwnerId(Long ownerId) {
        var owner = clientRepository.findById(ownerId).orElseThrow();
        var vehicles = vehicleRepository.findAllByOwner(owner);
        return vehicleMapper.toResponseList(vehicles);
    }

    private Vehicle saveVehicle(Vehicle vehicle) {
        var savedVehicle =  vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
        if(savedVehicle.isPresent() && !vehicle.equals(savedVehicle.get())) {
            // TODO criar a execeção
            throw new RuntimeException();
        } else {
            return vehicleRepository.save(vehicle);
        }
    }

    private void validateVehicleYear(int year) {
        if(year > LocalDate.now().getYear()) {
            throw new RuntimeException();
        }
    }
}
