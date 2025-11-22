package com.eddy.dream.service;

import com.eddy.dream.dto.request.UpdateUserRequest;
import com.eddy.dream.dto.response.PageResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.entity.UserEntity;
import com.eddy.dream.enums.UserStatus;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.ResourceNotFoundException;
import com.eddy.dream.mapper.UserMapper;
import com.eddy.dream.repository.UserRepository;
import com.eddy.dream.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private UserResponse userResponse;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phone("+1234567890")
            .password("encodedPassword")
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

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
    }


    @Test
    @DisplayName("Get User By ID - Success")
    void testGetUserByIdSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToResponse(userEntity)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).entityToResponse(userEntity);
    }

    @Test
    @DisplayName("Get User By ID - Not Found")
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserById(999L)
        );

        assertTrue(exception.getMessage().contains("User"));
        verify(userRepository, times(1)).findById(999L);
        verify(userMapper, never()).entityToResponse(any(UserEntity.class));
    }


    @Test
    @DisplayName("Get User By Username - Success")
    void testGetUserByUsernameSuccess() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToResponse(userEntity)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userMapper, times(1)).entityToResponse(userEntity);
    }

    @Test
    @DisplayName("Get User By Username - Not Found")
    void testGetUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }


    @Test
    @DisplayName("Get All Users - Success")
    void testGetAllUsersSuccess() {
        int page = 0;
        int size = 10;

        List<UserEntity> entities = Arrays.asList(userEntity);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> userPage = new PageImpl<>(entities, pageable, entities.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.entityToResponse(userEntity)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getAllUsers(page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        assertEquals(page, result.getPageNumber());
        assertEquals(size, result.getPageSize());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertFalse(result.isHasPrevious());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verify(userMapper, times(1)).entityToResponse(userEntity);
    }

    @Test
    @DisplayName("Get All Users - Empty Page")
    void testGetAllUsersEmpty() {
        int page = 0;
        int size = 10;

        List<UserEntity> entities = Collections.emptyList();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> userPage = new PageImpl<>(entities, pageable, 0);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserResponse> result = userService.getAllUsers(page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages()); // PageImpl 在 total=0 時 totalPages=0

        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Get All Users With Pagination - Success")
    void testGetAllUsersWithPaginationSuccess() {
        // Given
        UserEntity user2 = UserEntity.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .status(UserStatus.ACTIVE)
            .build();

        UserResponse response2 = UserResponse.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .status("ACTIVE")
            .build();

        Page<UserEntity> page = new PageImpl<>(
            Arrays.asList(userEntity, user2),
            PageRequest.of(0, 10),
            2
        );

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.entityToResponse(userEntity)).thenReturn(userResponse);
        when(userMapper.entityToResponse(user2)).thenReturn(response2);

        // When
        PageResponse<UserResponse> result = userService.getAllUsers(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verify(userMapper, times(2)).entityToResponse(any(UserEntity.class));
    }

    @Test
    @DisplayName("Get All Users With Pagination - Empty Page")
    void testGetAllUsersWithPaginationEmpty() {
        Page<UserEntity> emptyPage = new PageImpl<>(
            Arrays.asList(),
            PageRequest.of(0, 10),
            0
        );

        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<UserResponse> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Get All Users With Pagination - Verifies Sorting")
    void testGetAllUsersWithPaginationSorting() {
        Page<UserEntity> page = new PageImpl<>(Arrays.asList(userEntity));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        userService.getAllUsers(0, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
        assertNotNull(capturedPageable.getSort());
    }

    @Test
    @DisplayName("Update Last Login - Success")
    void testUpdateLastLoginSuccess() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // When
        userService.updateLastLogin("testuser");

        // Then
        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(entityCaptor.capture());
        
        UserEntity savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getLastLoginAt());
    }

    @Test
    @DisplayName("Update Last Login - User Not Found")
    void testUpdateLastLoginUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateLastLogin("nonexistent")
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Success (Email and Phone)")
    void testUpdateUserSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        
        UserResponse updatedResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("newemail@example.com")
            .phone("+9876543210")
            .status("ACTIVE")
            .build();
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(updatedResponse);

        UserResponse result = userService.updateUser(1L, updateUserRequest);

        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("+9876543210", result.getPhone());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Email Only")
    void testUpdateUserEmailOnly() {
        UpdateUserRequest emailOnlyRequest = new UpdateUserRequest();
        emailOnlyRequest.setEmail("newemail@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, emailOnlyRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Phone Only")
    void testUpdateUserPhoneOnly() {
        UpdateUserRequest phoneOnlyRequest = new UpdateUserRequest();
        phoneOnlyRequest.setPhone("+9876543210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, phoneOnlyRequest);

        assertNotNull(result);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - User Not Found")
    void testUpdateUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateUser(999L, updateUserRequest)
        );

        assertTrue(exception.getMessage().contains("User"));
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Email Already Exists")
    void testUpdateUserEmailExists() {
        UserEntity anotherUser = UserEntity.builder()
            .id(2L)
            .username("anotheruser")
            .email("newemail@example.com")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("newemail@example.com"))
            .thenReturn(Optional.of(anotherUser));

        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> userService.updateUser(1L, updateUserRequest)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("newemail@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Same Email (No Change)")
    void testUpdateUserSameEmail() {
        UpdateUserRequest sameEmailRequest = new UpdateUserRequest();
        sameEmailRequest.setEmail("test@example.com"); // Same as current

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(userEntity)); // Returns same user
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, sameEmailRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update User - Blank Email Ignored")
    void testUpdateUserBlankEmail() {
        UpdateUserRequest blankEmailRequest = new UpdateUserRequest();
        blankEmailRequest.setEmail("   "); // Blank

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        userService.updateUser(1L, blankEmailRequest);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Update User - Blank Phone Ignored")
    void testUpdateUserBlankPhone() {
        UpdateUserRequest blankPhoneRequest = new UpdateUserRequest();
        blankPhoneRequest.setPhone("   "); // Blank

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.entityToResponse(any(UserEntity.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, blankPhoneRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}

