package com.github.daniellevieira.vehiclemanagementapi.repository;

import com.github.daniellevieira.vehiclemanagementapi.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
