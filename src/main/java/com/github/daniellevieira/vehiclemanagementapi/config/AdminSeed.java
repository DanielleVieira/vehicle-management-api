package com.github.daniellevieira.vehiclemanagementapi.config;

import com.github.daniellevieira.vehiclemanagementapi.model.auth.Role;
import com.github.daniellevieira.vehiclemanagementapi.model.auth.User;
import com.github.daniellevieira.vehiclemanagementapi.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdminSeed implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeed(
            UserRepository  userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin-username}") String adminUsername,
            @Value("${app.admin-password}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = Objects.requireNonNull(adminUsername, "adminUsername cannot be null");
        this.adminPassword = Objects.requireNonNull(adminPassword,  "adminPassword cannot be null");
    }

    @Override
    public void run(String... args) throws Exception {
        var savedAdmin = userRepository.findByEmail(adminUsername);
        if(savedAdmin.isEmpty()) {
            var admin = new User(adminUsername, passwordEncoder.encode(adminPassword));
            userRepository.save(admin.promoteToAdmin());
            // TODO log de usuário admin criado
        } else if (!savedAdmin.get().getRole().equals(Role.ADMIN)) {
            // TODO log de usuário admin promovido a ADMIN
            userRepository.save(savedAdmin.get().promoteToAdmin());
        }
    }
}
