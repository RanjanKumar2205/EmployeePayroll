package org.example.employeepayroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void accessProtectedEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/employees/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    void searchEmployees_withEmployeeAuthority_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/employees/search"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "HR")
    void deleteEmployee_withHrAuthority_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteEmployee_withAdminAuthority_shouldNotReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().is(not(403)));
    }
}