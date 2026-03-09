package com.example.demo.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignUpRequest;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public UserResponse signUp(UserSignUpRequest request, String ipAddress) {
		userRepository.findByUserId(request.getUserId()).ifPresent(user -> {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists.");
		});

		User user = new User();
		user.setUserId(request.getUserId());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getName());
		user.setCreatedAt(LocalDateTime.now());
		user.setIpAddress(normalizeIpv4(ipAddress));

		userRepository.insert(user);
		return toResponse(user);
	}

	@Override
	public LoginResponse login(UserLoginRequest request) {
		User user = getExistingUser(request.getUserId());
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
		}
		return new LoginResponse(true, toResponse(user));
	}

	@Override
	public UserResponse getUser(String userId) {
		return toResponse(getExistingUser(userId));
	}

	@Override
	@Transactional
	public UserResponse updateUser(String userId, UserUpdateRequest request) {
		User user = getExistingUser(userId);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getName());

		userRepository.update(user);
		return toResponse(userRepository.findByUserId(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")));
	}

	@Override
	@Transactional
	public void deleteUser(String userId) {
		if (userRepository.deleteByUserId(userId) == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
		}
	}

	private User getExistingUser(String userId) {
		return userRepository.findByUserId(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(user.getUserId(), user.getName(), user.getCreatedAt(), user.getIpAddress());
	}

	private String normalizeIpv4(String ipAddress) {
		String candidate = ipAddress == null ? "" : ipAddress.trim();
		if ("::1".equals(candidate) || "0:0:0:0:0:0:0:1".equals(candidate)) {
			return "127.0.0.1";
		}
		if (candidate.startsWith("::ffff:")) {
			candidate = candidate.substring(7);
		}

		try {
			InetAddress address = InetAddress.getByName(candidate);
			if (address instanceof Inet4Address) {
				return address.getHostAddress();
			}
		}
		catch (UnknownHostException ignored) {
		}

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPv4 address is required.");
	}
}
