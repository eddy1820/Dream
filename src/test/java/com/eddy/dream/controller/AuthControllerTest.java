package com.eddy.dream.controller;

import com.eddy.dream.dto.request.LoginRequest;
import com.eddy.dream.dto.request.RegisterRequest;
import com.eddy.dream.dto.response.AuthResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.GlobalExceptionHandler;
import com.eddy.dream.exception.InvalidCredentialsException;
import com.eddy.dream.service.AuthService;
import com.eddy.dream.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * AuthController Integration Tests
 * Tests all authentication endpoints (register, login, health)
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("Password123!")
            .build();

        loginRequest = LoginRequest.builder()
            .username("testuser")
            .password("Password123!")
            .build();

        userResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .build();

        authResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .expiresIn(86400L)
            .user(userResponse)
            .build();
    }


    @Test
    @DisplayName("POST /api/auth/register - Success")
    void testRegisterSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token", is("jwt.token.here")))
            .andExpect(jsonPath("$.expiresIn", is(86400)))
            .andExpect(jsonPath("$.user.username", is("testuser")))
            .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Username Already Exists")
    void testRegisterUsernameExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new DuplicateResourceException("User", "username", "testuser"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("already exists")));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Email Already Exists")
    void testRegisterEmailExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new DuplicateResourceException("Email", "email", "test@example.com"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("already exists")));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Invalid Input (Missing Username)")
    void testRegisterInvalidInput() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .build(); // Missing username

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Invalid Email Format")
    void testRegisterInvalidEmailFormat() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
            .username("testuser")
            .email("invalid-email")
            .password("Password123!")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }


    @Test
    @DisplayName("POST /api/auth/login - Success")
    void testLoginSuccess() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("jwt.token.here")))
            .andExpect(jsonPath("$.expiresIn", is(86400)))
            .andExpect(jsonPath("$.user.username", is("testuser")));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid Credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new InvalidCredentialsException());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", containsString("Invalid")));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Missing Password")
    void testLoginMissingPassword() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
            .username("testuser")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }


    @Test
    @DisplayName("GET /api/auth/health - Success")
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("Auth service is running"));
    }
}

