package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest
class UserApiIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		jdbcTemplate.update("DELETE FROM users");
	}

	@Test
	void signupAndLoginShouldPersistUser() throws Exception {
		mockMvc.perform(post("/api/users/signup")
				.header("X-Forwarded-For", "192.168.0.10")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "userId": "tester01",
					  "password": "password123",
					  "name": "Tester"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").value("tester01"))
			.andExpect(jsonPath("$.name").value("Tester"))
			.andExpect(jsonPath("$.ipAddress").value("192.168.0.10"));

		Integer count = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM users WHERE user_id = ?",
			Integer.class,
			"tester01"
		);

		if (count == null || count != 1) {
			throw new AssertionError("User row was not inserted.");
		}

		mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "userId": "tester01",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authenticated").value(true))
			.andExpect(jsonPath("$.user.userId").value("tester01"));
	}

	@Test
	void userCrudFlowShouldWork() throws Exception {
		mockMvc.perform(post("/api/users/signup")
				.header("X-Forwarded-For", "10.10.10.10")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "userId": "crud01",
					  "password": "password123",
					  "name": "Before"
					}
					"""))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/users/crud01"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value("crud01"))
			.andExpect(jsonPath("$.name").value("Before"));

		mockMvc.perform(put("/api/users/crud01")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "password": "updated1234",
					  "name": "After"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("After"));

		mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "userId": "crud01",
					  "password": "updated1234"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authenticated").value(true));

		mockMvc.perform(delete("/api/users/crud01"))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/crud01"))
			.andExpect(status().isNotFound());
	}
}
