package com.eddy.dream.service;

import com.eddy.dream.dto.request.LoginRequest;
import com.eddy.dream.dto.request.RegisterRequest;
import com.eddy.dream.dto.response.AuthResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.entity.UserEntity;
import com.eddy.dream.enums.UserStatus;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.InvalidCredentialsException;
import com.eddy.dream.mapper.UserMapper;
import com.eddy.dream.repository.UserRepository;
import com.eddy.dream.service.impl.AuthServiceImpl;
import com.eddy.dream.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserEntity userEntity;
    private UserResponse userResponse;
    private UserDetails userDetails;

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

        userEntity = UserEntity.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword")
            .status(UserStatus.ACTIVE)
            .build();

        userResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .build();

        userDetails = new User(
            "testuser",
            "encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // ==================== Register Tests ====================

    @Test
    @DisplayName("Register - Success")
    void testRegisterSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt.token.here");
        when(jwtUtil.getExpiration()).thenReturn(86400L);
        when(userMapper.entityToResponse(userEntity)).thenReturn(userResponse);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(86400L, response.getExpiresIn());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    @DisplayName("Register - Username Already Exists")
    void testRegisterUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("username"));
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register - Email Already Exists")
    void testRegisterEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("email"));
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register - Password Encoding")
    void testRegisterPasswordEncoding() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("super.encoded.password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt.token");
        when(jwtUtil.getExpiration()).thenReturn(86400L);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        authService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("Password123!");
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("Login - Success")
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt.token.here");
        when(jwtUtil.getExpiration()).thenReturn(86400L);
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(86400L, response.getExpiresIn());
        assertEquals("testuser", response.getUser().getUsername());

        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(userService, times(1)).updateLastLogin("testuser");
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("Login - Invalid Credentials")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertNotNull(exception);
        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
        verify(userService, never()).updateLastLogin(anyString());
    }

    @Test
    @DisplayName("Login - Authentication Exception")
    void testLoginAuthenticationException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Authentication failed"));

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertNotNull(exception);
        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login - Updates Last Login Time")
    void testLoginUpdatesLastLoginTime() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt.token");
        when(jwtUtil.getExpiration()).thenReturn(86400L);
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        authService.login(loginRequest);

        verify(userService, times(1)).updateLastLogin("testuser");
    }

    @Test
    @DisplayName("Login - Generates JWT Token")
    void testLoginGeneratesJwtToken() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn("generated.jwt.token");
        when(jwtUtil.getExpiration()).thenReturn(86400L);
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        AuthResponse response = authService.login(loginRequest);

        assertEquals("generated.jwt.token", response.getToken());
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }
}

