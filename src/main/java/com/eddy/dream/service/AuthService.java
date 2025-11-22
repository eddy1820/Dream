package com.eddy.dream.service;

import com.eddy.dream.dto.request.LoginRequest;
import com.eddy.dream.dto.request.RegisterRequest;
import com.eddy.dream.dto.response.AuthResponse;


public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}

