package com.eddy.dream.controller;

import com.eddy.dream.dto.request.LoginRequest;
import com.eddy.dream.dto.request.RegisterRequest;
import com.eddy.dream.dto.response.AuthResponse;
import com.eddy.dream.dto.response.ErrorResponse;
import com.eddy.dream.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - Handles registration and login
 */
@Tag(name = "Authentication", description = "Authentication management APIs - Register and Login")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User Registration
     * 
     * POST /api/auth/register
     * 
     * @param request Registration request
     * @return Authentication response (including JWT Token)
     */
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account with username, email and password. Returns JWT token upon success."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Username or email already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * User Login
     * 
     * POST /api/auth/login
     * 
     * @param request Login request
     * @return Authentication response (including JWT Token)
     */
    @Operation(
        summary = "User login",
        description = "Authenticate user with username and password. Returns JWT token upon success."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health Check Endpoint
     * 
     * GET /api/auth/health
     * 
     * @return Status information
     */
    @Operation(
        summary = "Health check",
        description = "Check if the authentication service is running"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service is healthy"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}

