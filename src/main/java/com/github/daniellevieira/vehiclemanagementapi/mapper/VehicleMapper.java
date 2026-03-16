package com.github.daniellevieira.vehiclemanagementapi.mapper;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VehicleMapper {
    VehicleResponse toResponse(Vehicle entity);
}
