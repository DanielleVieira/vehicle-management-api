package com.github.daniellevieira.vehiclemanagementapi.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class UriUtils {
    private UriUtils() {
    }

    public static URI createLocationFromCurrentRequest(String id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
