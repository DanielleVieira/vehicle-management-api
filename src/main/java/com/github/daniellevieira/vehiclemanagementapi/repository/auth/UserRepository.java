package com.github.daniellevieira.vehiclemanagementapi.repository.auth;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
