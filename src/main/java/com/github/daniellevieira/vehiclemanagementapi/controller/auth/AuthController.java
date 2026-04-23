package com.github.daniellevieira.vehiclemanagementapi.controller.auth;

import com.github.daniellevieira.vehiclemanagementapi.dto.auth.LoginRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.LoginResponse;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.service.auth.UserService;
import com.github.daniellevieira.vehiclemanagementapi.util.UriUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> register(
            @Valid
            @RequestBody
            UserCreateRequest userCreateRequest
    ) {
        var loginResponse = userService.signUpUser(userCreateRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest loginRequest
    ) {
        var loginResponse = userService.logInUser(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}

