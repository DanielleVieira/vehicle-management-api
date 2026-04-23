package com.github.daniellevieira.vehiclemanagementapi.controller.auth;

import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserResponse;
import com.github.daniellevieira.vehiclemanagementapi.service.auth.UserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable
            @NotNull
            @Positive(message = "must be greater than 0")
            Long userId
    ) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // TODO updateUser
    // TODO deleteUser
}
