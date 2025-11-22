package com.eddy.dream.controller;

import com.eddy.dream.dto.request.UpdateUserRequest;
import com.eddy.dream.dto.response.PageResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.GlobalExceptionHandler;
import com.eddy.dream.exception.ResourceNotFoundException;
import com.eddy.dream.service.UserService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    private UserResponse userResponse;
    private UpdateUserRequest updateUserRequest;
    private PageResponse<UserResponse> pageResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phone("+1234567890")
            .status("ACTIVE")
            .build();

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setEmail("newemail@example.com");
        updateUserRequest.setPhone("+9876543210");

        List<UserResponse> users = Arrays.asList(
            userResponse,
            UserResponse.builder().id(2L).username("user2").email("user2@example.com").status("ACTIVE").build()
        );

        pageResponse = PageResponse.<UserResponse>builder()
            .content(users)
            .pageNumber(0)
            .pageSize(10)
            .totalElements(2L)
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
    }


    @Test
    @DisplayName("GET /api/users/me - Success")
    @WithMockUser(username = "testuser")
    void testGetCurrentUserSuccess() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.phone", is("+1234567890")));

        verify(userService, times(1)).getUserByUsername("testuser");
    }


    @Test
    @DisplayName("GET /api/users/{id} - Success")
    @WithMockUser
    void testGetUserByIdSuccess() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id} - User Not Found")
    @WithMockUser
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(999L))
            .thenThrow(new ResourceNotFoundException("User", 999L));

        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        verify(userService, times(1)).getUserById(999L);
    }


    // ==================== Get All Users Tests ====================

    @Test
    @DisplayName("GET /api/users - Success with Pagination")
    @WithMockUser
    void testGetAllUsersSuccess() throws Exception {
        when(userService.getAllUsers(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].username", is("testuser")))
            .andExpect(jsonPath("$.pageNumber", is(0)))
            .andExpect(jsonPath("$.pageSize", is(10)))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.totalPages", is(1)));

        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    @DisplayName("GET /api/users - Default Pagination")
    @WithMockUser
    void testGetAllUsersDefaultPagination() throws Exception {
        when(userService.getAllUsers(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());

        verify(userService, times(1)).getAllUsers(0, 10);
    }


    // ==================== Update User Tests ====================

    @Test
    @DisplayName("PUT /api/users/{id} - Success")
    @WithMockUser
    void testUpdateUserSuccess() throws Exception {
        UserResponse updatedResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("newemail@example.com")
            .phone("+9876543210")
            .status("ACTIVE")
            .build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("newemail@example.com")))
            .andExpect(jsonPath("$.phone", is("+9876543210")));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - User Not Found")
    @WithMockUser
    void testUpdateUserNotFound() throws Exception {
        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
            .thenThrow(new ResourceNotFoundException("User", 999L));

        mockMvc.perform(put("/api/users/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        verify(userService, times(1)).updateUser(eq(999L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Email Already Exists")
    @WithMockUser
    void testUpdateUserEmailExists() throws Exception {
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
            .thenThrow(new DuplicateResourceException("Email already exists: newemail@example.com"));

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("already exists")));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Invalid Email Format")
    @WithMockUser
    void testUpdateUserInvalidEmail() throws Exception {
        UpdateUserRequest invalidRequest = new UpdateUserRequest();
        invalidRequest.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Invalid Phone Format")
    @WithMockUser
    void testUpdateUserInvalidPhone() throws Exception {
        UpdateUserRequest invalidRequest = new UpdateUserRequest();
        invalidRequest.setPhone("123"); // Too short

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

}

