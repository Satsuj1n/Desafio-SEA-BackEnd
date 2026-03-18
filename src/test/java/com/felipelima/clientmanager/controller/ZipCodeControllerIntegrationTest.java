package com.felipelima.clientmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipelima.clientmanager.dto.request.LoginRequest;

/**
 * Integration tests for ZipCodeController.
 *
 * Note: These tests depend on external CEP APIs (ViaCEP / OpenCEP).
 * If both providers are unavailable, some tests may fail.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ZipCodeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "123qwel@#");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = json.get("token").asText();
    }

    @Test
    @DisplayName("GET /cep/{zipCode} - Should return 200 for valid zip code")
    void lookupValidZipCode() throws Exception {
        mockMvc.perform(get("/cep/{zipCode}", "70040010")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").isNotEmpty())
                .andExpect(jsonPath("$.city").isNotEmpty())
                .andExpect(jsonPath("$.state").isNotEmpty());
    }

    @Test
    @DisplayName("GET /cep/{zipCode} - Should accept zip code with mask")
    void lookupZipCodeWithMask() throws Exception {
        mockMvc.perform(get("/cep/{zipCode}", "70040-010")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").isNotEmpty());
    }

    @Test
    @DisplayName("GET /cep/{zipCode} - Should return 400 for invalid zip code")
    void lookupInvalidZipCode() throws Exception {
        mockMvc.perform(get("/cep/{zipCode}", "00000000")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /cep/{zipCode} - Should return 403 without token")
    void lookupNoToken() throws Exception {
        mockMvc.perform(get("/cep/{zipCode}", "70040010"))
                .andExpect(status().isForbidden());
    }
}
