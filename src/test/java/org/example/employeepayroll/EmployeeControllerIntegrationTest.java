package org.example.employeepayroll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.employeepayroll.dtos.EmployeeRequestDto;
import org.example.employeepayroll.entities.EmployeeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ── KEY FIX 1: MOCK env, not RANDOM_PORT ─────────────────────────────────────
// RANDOM_PORT starts a real HTTP server. MockMvc talks to it over HTTP, which
// means @WithMockUser's SecurityContext never reaches the live filter chain and
// every request arrives unauthenticated → 401.
// MOCK env wires MockMvc directly through the DispatcherServlet in-process,
// so SecurityMockMvcConfigurers.springSecurity() can inject the mock principal.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@Sql("/test-data.sql")
class EmployeeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    // ── KEY FIX 2: build MockMvc manually with springSecurity() applied ───────
    // @AutoConfigureMockMvc auto-wires a MockMvc bean but does NOT apply
    // SecurityMockMvcConfigurers.springSecurity(), which is the configurer that
    // bridges @WithMockUser into the filter chain. Without it, @WithMockUser is
    // silently ignored. Building manually here mirrors the working pattern
    // already used in SecurityTest in this same project.
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())   // ← this is what makes @WithMockUser work
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    // ── Test 1: POST /employees with valid body → 201 + body has id ──────────

    @Test
    @DisplayName("POST /employees/ with valid body → 201 and response body contains an id")
    @WithMockUser(authorities = "ADMIN")
    void createEmployee_validRequest_returns201WithId() throws Exception {
        EmployeeRequestDto request = EmployeeRequestDto.builder()
                .employeeCode("EMP200")
                .firstName("Ankita")
                .lastName("Verma")
                .email("ankita.verma@test.com")
                .phoneNumber("9123456780")
                .designation("QA Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2025, 3, 1))
                .departmentId(100L)
                .build();

        mockMvc.perform(post("/api/v1/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ankita.verma@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── Test 2: POST with invalid email → 400 + error message ────────────────

    @Test
    @DisplayName("POST /employees/ with invalid email → 400 and error message in response")
    @WithMockUser(authorities = "ADMIN")
    void createEmployee_invalidEmail_returns400WithErrorMessage() throws Exception {
        EmployeeRequestDto request = EmployeeRequestDto.builder()
                .employeeCode("EMP201")
                .firstName("Bad")
                .lastName("Request")
                .email("not-a-valid-email")
                .phoneNumber("9123456781")
                .designation("Developer")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2025, 1, 1))
                .departmentId(100L)
                .build();

        mockMvc.perform(post("/api/v1/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    // ── Test 3: GET /employees/{validId} → 200 + correct data ────────────────

    @Test
    @DisplayName("GET /employees/{id} with valid id → 200 and correct employee data")
    @WithMockUser(authorities = "ADMIN")
    void getEmployee_validId_returns200WithCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/employees/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Rahul"))
                .andExpect(jsonPath("$.lastName").value("Sharma"))
                .andExpect(jsonPath("$.email").value("rahul.sharma@test.com"))
                .andExpect(jsonPath("$.departmentId").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── Test 4: GET /employees/99999 → 404 ───────────────────────────────────

    @Test
    @DisplayName("GET /employees/{id} with non-existent id → 404")
    @WithMockUser(authorities = "ADMIN")
    void getEmployee_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("99999")));
    }
}