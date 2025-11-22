package com.eddy.dream.service.impl;

import com.eddy.dream.aspect.annotation.LogExecutionTime;
import com.eddy.dream.dto.request.UpdateUserRequest;
import com.eddy.dream.dto.response.PageResponse;
import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.entity.UserEntity;
import com.eddy.dream.exception.DuplicateResourceException;
import com.eddy.dream.exception.ResourceNotFoundException;
import com.eddy.dream.mapper.UserMapper;
import com.eddy.dream.repository.UserRepository;
import com.eddy.dream.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        return userMapper.entityToResponse(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        
        UserEntity entity = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        return userMapper.entityToResponse(entity);
    }

    
    @Override
    @Transactional(readOnly = true)
    @LogExecutionTime(value = "Get All Users with Pagination", logParams = true, logResult = true, threshold = 1000)
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        log.debug("Getting users with pagination - page: {}, size: {}", page, size);

        // Create pageable with sorting by ID descending (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        // Get page from repository
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        
        // Convert entities to DTOs
        List<UserResponse> userResponses = userPage.getContent().stream()
            .map(userMapper::entityToResponse)
            .collect(Collectors.toList());
        
        // Build page response
        return PageResponse.<UserResponse>builder()
            .content(userResponses)
            .pageNumber(userPage.getNumber())
            .pageSize(userPage.getSize())
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .hasNext(userPage.hasNext())
            .hasPrevious(userPage.hasPrevious())
            .build();
    }
    
    @Override
    @Transactional
    public void updateLastLogin(String username) {
        log.debug("Updating last login time for user: {}", username);
        
        UserEntity entity = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        entity.setLastLoginAt(LocalDateTime.now());
        userRepository.save(entity);
    }
    
    @Override
    @Transactional
    @LogExecutionTime(value = "Update User Information", logParams = true, logResult = true)
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.debug("Updating user information for ID: {}", id);
        
        // Find user
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if email is already taken by another user
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new DuplicateResourceException("Email already exists: " + request.getEmail());
                }
            });
            
            entity.setEmail(request.getEmail());
            log.info("Updated email for user ID {}: {}", id, request.getEmail());
        }
        
        // Update phone if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            entity.setPhone(request.getPhone());
            log.info("Updated phone for user ID {}: {}", id, request.getPhone());
        }
        
        // Save updated entity
        UserEntity updatedEntity = userRepository.save(entity);
        
        return userMapper.entityToResponse(updatedEntity);
    }
}

