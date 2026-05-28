package com.github.daniellevieira.vehiclemanagementapi.mapper;

import com.github.daniellevieira.vehiclemanagementapi.dto.VehicleResponse;
import com.github.daniellevieira.vehiclemanagementapi.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

//usa ClientMapper para fazer a conversão de owner
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ClientMapper.class)
public interface VehicleMapper {
    VehicleResponse toResponse(Vehicle entity);
    List<VehicleResponse> toResponseList(List<Vehicle> entity);
}
