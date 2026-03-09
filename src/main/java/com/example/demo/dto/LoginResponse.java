package com.example.demo.dto;

public class LoginResponse {

	private final boolean authenticated;
	private final UserResponse user;

	public LoginResponse(boolean authenticated, UserResponse user) {
		this.authenticated = authenticated;
		this.user = user;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public UserResponse getUser() {
		return user;
	}
}
