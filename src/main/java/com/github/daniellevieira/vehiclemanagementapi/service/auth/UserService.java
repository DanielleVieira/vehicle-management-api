package com.github.daniellevieira.vehiclemanagementapi.service.auth;

import com.github.daniellevieira.vehiclemanagementapi.dto.auth.LoginRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.LoginResponse;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserCreateRequest;
import com.github.daniellevieira.vehiclemanagementapi.dto.auth.UserResponse;
import com.github.daniellevieira.vehiclemanagementapi.exception.DuplicateResourceException;
import com.github.daniellevieira.vehiclemanagementapi.exception.ResourceNotFoundException;
import com.github.daniellevieira.vehiclemanagementapi.mapper.auth.UserMapper;
import com.github.daniellevieira.vehiclemanagementapi.model.auth.User;
import com.github.daniellevieira.vehiclemanagementapi.repository.auth.UserRepository;
import com.github.daniellevieira.vehiclemanagementapi.security.auth.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String BEARER = "Bearer";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            UserMapper userMapper,
            JwtService jwtService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    // TODO tratar exceções
    public LoginResponse signUpUser(UserCreateRequest userCreateRequest) {
        var newUser = userMapper.toEntity(userCreateRequest);
        var savedUser = saveUser(newUser);
        return buildLoginResponse(savedUser);
    }

    // TODO tratar exceções
    public LoginResponse logInUser(LoginRequest loginRequest) {
        authenticationManager.authenticate( // Lança AuthenticationException se a autenticação falha
            new UsernamePasswordAuthenticationToken(
                    loginRequest.email(),
                    loginRequest.password()
            )
        );
        var authenticatedUser = userRepository
                .findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + loginRequest.email() + " not found")); // Se passou na authenticação então o usuário existe
        return buildLoginResponse(authenticatedUser);
    }

    // TODO tratar exceções
    private LoginResponse buildLoginResponse(User authenticatedUser) {
        String token =  jwtService.generateToken(authenticatedUser);
        return new LoginResponse(token, BEARER, userMapper.toResponse(authenticatedUser));
    }

    // TODO tratar exceções
    private User saveUser(User newUser) {
        var savedUser = userRepository
                .findByEmail(newUser.getEmail());

        if (savedUser.isPresent() && !newUser.equals(savedUser.get())) {
            throw new DuplicateResourceException("Email already registered");
        } else {
            var encodedPassword =  passwordEncoder.encode(newUser.getPassword());
            return userRepository.save(newUser.updateUser(newUser.getEmail(), encodedPassword, newUser.getRole()));
        }
    }

    public UserResponse getUser(Long userId) {
        var user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        return userMapper.toResponse(user);
    }

    // TODO updateUser
    // TODO deleteUser
}
