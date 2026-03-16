package com.github.daniellevieira.vehiclemanagementapi.mapper;

import com.github.daniellevieira.vehiclemanagementapi.dto.ClientCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.ClientResponse;
import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) // para transformar em um bean
public interface ClientMapper {
    Client toEntity(ClientCreateRequest dto);
    ClientResponse toResponse(Client client);
}
