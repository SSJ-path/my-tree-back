package com.example.demo.service;

import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignUpRequest;
import com.example.demo.dto.UserUpdateRequest;

public interface UserService {

	UserResponse signUp(UserSignUpRequest request, String ipAddress);

	LoginResponse login(UserLoginRequest request);

	UserResponse getUser(String userId);

	UserResponse updateUser(String userId, UserUpdateRequest request);

	void deleteUser(String userId);
}
