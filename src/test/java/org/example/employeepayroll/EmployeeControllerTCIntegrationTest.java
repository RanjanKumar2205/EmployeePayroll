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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests using a REAL MySQL container via TestContainers.
 * The container starts once for the whole class (@Container static) and is
 * shared across all tests — faster than one container per test.
 *
 * Key difference from EmployeeControllerIntegrationTest (H2):
 *  - Real MySQL dialect and behaviour (no H2 quirks)
 *  - Flyway runs actual migration scripts V1–V5
 *  - docker ps during the test run shows a mysql:8.0 container
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("testcontainers")
@Transactional
@Sql("/test-data.sql")
class EmployeeControllerTCIntegrationTest {

    // ── Container — static so it's shared across all tests in this class ──────
    // TestContainers starts it before the Spring context boots and stops it
    // after all tests in the class finish.
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.40")
            .withDatabaseName("payroll_test")
            .withUsername("testuser")
            .withPassword("testpass");

    // ── @DynamicPropertySource — overrides datasource config at runtime ───────
    // Spring calls this BEFORE creating the ApplicationContext, so the live
    // container's URL/port are injected before any bean tries to connect.
    @DynamicPropertySource
    static void overrideDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[TC] POST /employees/ with valid body → 201 and id in response")
    @WithMockUser(authorities = "ADMIN")
    void createEmployee_validRequest_returns201() throws Exception {
        EmployeeRequestDto request = EmployeeRequestDto.builder()
                .employeeCode("EMP300")
                .firstName("Vikram")
                .lastName("Nair")
                .email("vikram.nair@test.com")
                .phoneNumber("9000000001")
                .designation("DevOps Engineer")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2025, 5, 1))
                .departmentId(100L)
                .build();

        mockMvc.perform(post("/api/v1/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("vikram.nair@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("[TC] POST /employees/ with invalid email → 400")
    @WithMockUser(authorities = "ADMIN")
    void createEmployee_invalidEmail_returns400() throws Exception {
        EmployeeRequestDto request = EmployeeRequestDto.builder()
                .employeeCode("EMP301")
                .firstName("Bad")
                .lastName("Data")
                .email("not-an-email")
                .phoneNumber("9000000002")
                .designation("Tester")
                .employeeType(EmployeeType.FULL_TIME)
                .dateOfJoining(LocalDate.of(2025, 1, 1))
                .departmentId(100L)
                .build();

        mockMvc.perform(post("/api/v1/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    @DisplayName("[TC] GET /employees/100 → 200 and correct seeded data")
    @WithMockUser(authorities = "ADMIN")
    void getEmployee_seededId_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/employees/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Rahul"))
                .andExpect(jsonPath("$.email").value("rahul.sharma@test.com"));
    }

    @Test
    @DisplayName("[TC] GET /employees/99999 → 404 with id in message")
    @WithMockUser(authorities = "ADMIN")
    void getEmployee_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("99999")));
    }
}