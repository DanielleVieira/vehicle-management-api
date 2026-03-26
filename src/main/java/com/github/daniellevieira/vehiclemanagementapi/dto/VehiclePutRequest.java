package com.github.daniellevieira.vehiclemanagementapi.dto;

import jakarta.validation.constraints.*;

public record VehiclePutRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String make,

        @NotBlank
        @Size(min = 3, max = 100)
        String model,

        @NotNull
        @Min(1886)
        Integer year,

        @NotBlank
        @Size(min = 7, max = 8)
        @Pattern(
                regexp = "(?i)([A-Z]{3}-?[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})",
                message = "Deve seguir o padrão brasileiro antigo ou Mercosul"
        )
        String licensePlate
) implements VehicleRequest { }
