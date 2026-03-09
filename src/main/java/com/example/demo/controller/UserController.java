package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignUpRequest;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup")
	public ResponseEntity<UserResponse> signUp(
		@Valid @RequestBody UserSignUpRequest request,
		@RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
		HttpServletRequest httpServletRequest
	) {
		String ipAddress = extractClientIp(forwardedFor, httpServletRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.signUp(request, ipAddress));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
		return ResponseEntity.ok(userService.login(request));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<UserResponse> updateUser(
		@PathVariable String userId,
		@Valid @RequestBody UserUpdateRequest request
	) {
		return ResponseEntity.ok(userService.updateUser(userId, request));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	private String extractClientIp(String forwardedFor, HttpServletRequest request) {
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			List<String> forwardedIps = List.of(forwardedFor.split(","));
			if (!forwardedIps.isEmpty()) {
				return forwardedIps.getFirst().trim();
			}
		}
		return request.getRemoteAddr();
	}
}
