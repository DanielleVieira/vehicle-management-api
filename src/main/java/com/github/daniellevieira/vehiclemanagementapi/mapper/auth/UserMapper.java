package com.github.daniellevieira.vehiclemanagementapi.mapper.auth;

import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserResponse;
import com.github.daniellevieira.vehiclemanagementapi.model.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UserCreateRequest dto);
    UserResponse toResponse(User user);
}
