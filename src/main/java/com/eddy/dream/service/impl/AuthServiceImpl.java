package com.eddy.dream.service.impl;

import com.eddy.dream.enums.UserStatus;
import com.eddy.dream.dto.request.LoginRequest;
import com.eddy.dream.dto.request.RegisterRequest;
import com.eddy.dream.dto.response.AuthResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.entity.UserEntity;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.InvalidCredentialsException;
import com.eddy.dream.mapper.UserMapper;
import com.eddy.dream.repository.UserRepository;
import com.eddy.dream.service.AuthService;
import com.eddy.dream.service.UserService;
import com.eddy.dream.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email", "email", request.getEmail());
        }
        
        // Create entity and save to database
        UserEntity entity = UserEntity.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .status(UserStatus.ACTIVE)
            .build();
        
        UserEntity savedEntity = userRepository.save(entity);
        
        // Generate JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedEntity.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        
        // Build response
        UserResponse userResponse = userMapper.entityToResponse(savedEntity);
        
        log.info("User registered successfully: {}", savedEntity.getUsername());
        
        return AuthResponse.of(token, jwtUtil.getExpiration(), userResponse);
    }
    
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User logging in: {}", request.getUsername());
        
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // Authentication successful, generate JWT Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // Update last login time
            userService.updateLastLogin(request.getUsername());
            
            // Get user information
            UserResponse userResponse = userService.getUserByUsername(request.getUsername());
            
            log.info("User logged in successfully: {}", request.getUsername());
            
            return AuthResponse.of(token, jwtUtil.getExpiration(), userResponse);
            
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new InvalidCredentialsException();
        }
    }
}

