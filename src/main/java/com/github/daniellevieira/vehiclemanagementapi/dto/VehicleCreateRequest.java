package com.github.daniellevieira.vehiclemanagementapi.dto;

import jakarta.validation.constraints.*;

public record VehicleCreateRequest(
        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 50, message = "size must be between 3 and 50")
        String make,

        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 100, message = "size must be between 3 and 100")
        String model,

        @NotNull(message = "must not be null")
        @Min(value = 1886, message = "must be greater than or equal to 1886")
        Integer year,

        @NotBlank(message = "must not be blank")
        @Pattern(
                regexp = "(?i)([A-Z]{3}-?[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})",
                message = "must follow the old Brazilian standard or Mercosur standard."
        )
        String licensePlate,

        @NotNull(message = "must not be null")
        @Positive(message = "must be greater than 0")
        Long ownerId
) implements VehicleRequest { }
