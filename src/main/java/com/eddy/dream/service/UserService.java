package com.eddy.dream.service;

import com.eddy.dream.dto.request.UpdateUserRequest;
import com.eddy.dream.dto.response.PageResponse;
import com.eddy.dream.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    PageResponse<UserResponse> getAllUsers(int page, int size);

    void updateLastLogin(String username);

    UserResponse updateUser(Long id, UpdateUserRequest request);
}

