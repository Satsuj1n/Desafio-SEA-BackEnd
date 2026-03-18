package com.felipelima.clientmanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipelima.clientmanager.dto.request.AddressRequest;
import com.felipelima.clientmanager.dto.request.ClientRequest;
import com.felipelima.clientmanager.dto.request.EmailRequest;
import com.felipelima.clientmanager.dto.request.LoginRequest;
import com.felipelima.clientmanager.dto.request.PhoneRequest;
import com.felipelima.clientmanager.repository.ClientRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        clientRepository.deleteAll();
        adminToken = obtainToken("admin", "123qwel@#");
        userToken = obtainToken("user", "123qwe123");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private ClientRequest buildValidClientRequest() {
        AddressRequest address = new AddressRequest(
                "70040-010", "SBS Quadra 2", "Asa Sul", "Brasilia", "DF", null);
        PhoneRequest phone = new PhoneRequest("MOBILE", "(61) 99999-8888");
        EmailRequest email = new EmailRequest("joao@email.com");

        return new ClientRequest(
                "Joao Silva",
                "123.456.789-09",
                address,
                Collections.singletonList(phone),
                Collections.singletonList(email));
    }

    private Long createClientAndReturnId() throws Exception {
        ClientRequest request = buildValidClientRequest();

        MvcResult result = mockMvc.perform(post("/clients")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    // ==========================================
    // CREATE - POST /clients
    // ==========================================

    @Nested
    @DisplayName("POST /clients")
    class CreateTests {

        @Test
        @DisplayName("201 - Should create client successfully as ADMIN")
        void createSuccess() throws Exception {
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("Joao Silva"))
                    .andExpect(jsonPath("$.cpf").value("123.456.789-09"))
                    .andExpect(jsonPath("$.address.zipCode").value("70040-010"))
                    .andExpect(jsonPath("$.phones[0].number").value("(61) 99999-8888"))
                    .andExpect(jsonPath("$.phones[0].type").value("MOBILE"))
                    .andExpect(jsonPath("$.emails[0].address").value("joao@email.com"));
        }

        @Test
        @DisplayName("400 - Should return validation errors for empty body")
        void createValidationErrors() throws Exception {
            ClientRequest request = new ClientRequest(
                    "", "", null, Collections.emptyList(), Collections.emptyList());

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(3))));
        }

        @Test
        @DisplayName("400 - Should reject name shorter than 3 characters")
        void createNameTooShort() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setName("AB");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject name with special characters")
        void createNameWithSpecialChars() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setName("Joao @Silva!");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject invalid email format")
        void createInvalidEmail() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setEmails(Collections.singletonList(new EmailRequest("not-an-email")));

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject duplicate CPF")
        void createDuplicateCpf() throws Exception {
            createClientAndReturnId();

            ClientRequest request = buildValidClientRequest();
            request.setName("Outro Nome");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("CPF already registered")));
        }

        @Test
        @DisplayName("400 - Should reject state with more than 2 characters")
        void createInvalidStateLength() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.getAddress().setState("DFF");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject invalid CPF (wrong check digits)")
        void createInvalidCpf() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setCpf("123.456.789-00");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject CPF with all identical digits")
        void createCpfAllSameDigits() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setCpf("111.111.111-11");

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject invalid phone type")
        void createInvalidPhoneType() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setPhones(Collections.singletonList(new PhoneRequest("INVALID", "61999998888")));

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 - Should reject null address")
        void createNullAddress() throws Exception {
            ClientRequest request = buildValidClientRequest();
            request.setAddress(null);

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("403 - Should deny creation for USER role")
        void createForbiddenForUser() throws Exception {
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 - Should deny creation without token")
        void createNoToken() throws Exception {
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(post("/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // LIST - GET /clients
    // ==========================================

    @Nested
    @DisplayName("GET /clients")
    class FindAllTests {

        @Test
        @DisplayName("200 - Should list clients as ADMIN")
        void findAllAsAdmin() throws Exception {
            createClientAndReturnId();

            mockMvc.perform(get("/clients")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].cpf").value("123.456.789-09"));
        }

        @Test
        @DisplayName("200 - Should list clients as USER (read-only)")
        void findAllAsUser() throws Exception {
            createClientAndReturnId();

            mockMvc.perform(get("/clients")
                    .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("200 - Should return empty list when no clients")
        void findAllEmpty() throws Exception {
            mockMvc.perform(get("/clients")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("403 - Should deny listing without token")
        void findAllNoToken() throws Exception {
            mockMvc.perform(get("/clients"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // FIND BY ID - GET /clients/{id}
    // ==========================================

    @Nested
    @DisplayName("GET /clients/{id}")
    class FindByIdTests {

        @Test
        @DisplayName("200 - Should find client by ID as ADMIN")
        void findByIdAsAdmin() throws Exception {
            Long id = createClientAndReturnId();

            mockMvc.perform(get("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Joao Silva"))
                    .andExpect(jsonPath("$.cpf").value("123.456.789-09"));
        }

        @Test
        @DisplayName("200 - Should find client by ID as USER")
        void findByIdAsUser() throws Exception {
            Long id = createClientAndReturnId();

            mockMvc.perform(get("/clients/{id}", id)
                    .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        @DisplayName("404 - Should return not found for non-existent ID")
        void findByIdNotFound() throws Exception {
            mockMvc.perform(get("/clients/{id}", 999)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("999")));
        }

        @Test
        @DisplayName("403 - Should deny access without token")
        void findByIdNoToken() throws Exception {
            mockMvc.perform(get("/clients/{id}", 1))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // UPDATE - PUT /clients/{id}
    // ==========================================

    @Nested
    @DisplayName("PUT /clients/{id}")
    class UpdateTests {

        @Test
        @DisplayName("200 - Should update client successfully as ADMIN")
        void updateSuccess() throws Exception {
            Long id = createClientAndReturnId();

            ClientRequest updateRequest = new ClientRequest(
                    "Joao Silva Updated",
                    "123.456.789-09",
                    new AddressRequest("70040-010", "SBS Quadra 2 Bloco A", "Asa Sul", "Brasilia", "DF", "Sala 501"),
                    Collections.singletonList(new PhoneRequest("MOBILE", "(61) 99999-7777")),
                    Collections.singletonList(new EmailRequest("joao.novo@email.com")));

            mockMvc.perform(put("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Joao Silva Updated"))
                    .andExpect(jsonPath("$.address.complement").value("Sala 501"))
                    .andExpect(jsonPath("$.phones[0].number").value("(61) 99999-7777"))
                    .andExpect(jsonPath("$.emails[0].address").value("joao.novo@email.com"));
        }

        @Test
        @DisplayName("404 - Should return not found for non-existent ID")
        void updateNotFound() throws Exception {
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(put("/clients/{id}", 999)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("400 - Should reject update with validation errors")
        void updateValidationErrors() throws Exception {
            Long id = createClientAndReturnId();

            ClientRequest badRequest = new ClientRequest(
                    "", "", null, Collections.emptyList(), Collections.emptyList());

            mockMvc.perform(put("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("403 - Should deny update for USER role")
        void updateForbiddenForUser() throws Exception {
            Long id = createClientAndReturnId();
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(put("/clients/{id}", id)
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 - Should deny update without token")
        void updateNoToken() throws Exception {
            ClientRequest request = buildValidClientRequest();

            mockMvc.perform(put("/clients/{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // DELETE - DELETE /clients/{id}
    // ==========================================

    @Nested
    @DisplayName("DELETE /clients/{id}")
    class DeleteTests {

        @Test
        @DisplayName("204 - Should delete client successfully as ADMIN")
        void deleteSuccess() throws Exception {
            Long id = createClientAndReturnId();

            mockMvc.perform(delete("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 - Should return not found for non-existent ID")
        void deleteNotFound() throws Exception {
            mockMvc.perform(delete("/clients/{id}", 999)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Should deny delete for USER role")
        void deleteForbiddenForUser() throws Exception {
            Long id = createClientAndReturnId();

            mockMvc.perform(delete("/clients/{id}", id)
                    .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 - Should deny delete without token")
        void deleteNoToken() throws Exception {
            mockMvc.perform(delete("/clients/{id}", 1))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // MASK VALIDATION (end-to-end)
    // ==========================================

    @Nested
    @DisplayName("Data masking end-to-end")
    class MaskingTests {

        @Test
        @DisplayName("Response should contain masked CPF, zip code and phone")
        void responseMasksApplied() throws Exception {
            Long id = createClientAndReturnId();

            mockMvc.perform(get("/clients/{id}", id)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cpf").value("123.456.789-09"))
                    .andExpect(jsonPath("$.address.zipCode").value("70040-010"))
                    .andExpect(jsonPath("$.phones[0].number").value("(61) 99999-8888"));
        }

        @Test
        @DisplayName("Should accept input with or without masks")
        void acceptInputWithoutMasks() throws Exception {
            ClientRequest request = new ClientRequest(
                    "Maria Souza",
                    "98765432100",
                    new AddressRequest("01001000", "Praca da Se", "Se", "Sao Paulo", "SP", null),
                    Collections.singletonList(new PhoneRequest("RESIDENTIAL", "1133334444")),
                    Collections.singletonList(new EmailRequest("maria@email.com")));

            mockMvc.perform(post("/clients")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cpf").value("987.654.321-00"))
                    .andExpect(jsonPath("$.address.zipCode").value("01001-000"))
                    .andExpect(jsonPath("$.phones[0].number").value("(11) 3333-4444"));
        }
    }
}
