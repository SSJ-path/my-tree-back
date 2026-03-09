package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserSignUpRequest {

	@NotBlank
	@Size(max = 50)
	private String userId;

	@NotBlank
	@Size(min = 8, max = 100)
	private String password;

	@NotBlank
	@Size(max = 50)
	private String name;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
