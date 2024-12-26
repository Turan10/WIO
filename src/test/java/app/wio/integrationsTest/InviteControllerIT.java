/*
// File: src/test/java/app/wio/integrationsTest/InviteControllerIT.java

package app.wio.integrationsTest;

import app.wio.dto.response.InviteResponseDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.UserRepository;
import app.wio.security.TestJwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

*/
/**
 * InviteControllerIT contains integration tests for the InviteController.
 *//*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Ensure test profile is active
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Ensures transactional context for each test method
class InviteControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long existingCompanyId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clear any existing persistence context
        entityManager.clear();

        // Create a Company to be used in tests
        Company company = new Company();
        company.setName("Test Company");
        company.setAddress("123 Test St");

        company = companyRepository.save(company);
        existingCompanyId = company.getId();

        // Create an ADMIN User
        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setEnabled(true);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setCompany(company);

        adminUser = userRepository.save(adminUser);

        // Generate JWT Token for ADMIN User
        adminToken = testJwtTokenUtil.generateToken(adminUser);

        // Flush and clear the persistence context to ensure entities are loaded fresh
        entityManager.flush();
        entityManager.clear();

        // Verify company and user setup
        assertNotNull(existingCompanyId, "Created company should have an ID");
        assertTrue(companyRepository.findById(existingCompanyId).isPresent(), "Test company should exist");
        assertNotNull(adminUser.getId(), "Created admin user should have an ID");
        assertTrue(userRepository.findById(adminUser.getId()).isPresent(), "Admin user should exist");
    }

    */
/**
     * Tests successful creation of a bulk invite.
     *//*

    @Test
    void testCreateBulkInvite() throws Exception {
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", existingCompanyId.toString())
                        .param("expirationInHours", "24")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteLink").exists())
                .andExpect(jsonPath("$.companyId").value(existingCompanyId))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.joinedCount").value(0))
                // Check that joinedUsers is present and empty
                .andExpect(jsonPath("$.joinedUsers").isEmpty());
    }

    */
/**
     * Tests that an unauthorized request (without JWT) receives a 401 Unauthorized status.
     *//*

    @Test
    void testCreateBulkInvite_Unauthorized() throws Exception {
        // No Authorization header
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", existingCompanyId.toString())
                        .param("expirationInHours", "24")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    */
/**
     * Tests that a user without ADMIN privileges receives a 403 Forbidden status.
     *//*

    @Test
    void testCreateBulkInvite_Forbidden() throws Exception {
        // Create a USER role without ADMIN privileges
        User normalUser = new User();
        normalUser.setName("Employee User");
        normalUser.setEmail("employee@example.com");
        normalUser.setPassword(passwordEncoder.encode("password"));
        normalUser.setEnabled(true);
        normalUser.setRole(UserRole.EMPLOYEE);
        normalUser.setCompany(companyRepository.findById(existingCompanyId).orElseThrow());

        normalUser = userRepository.save(normalUser);

        String userToken = testJwtTokenUtil.generateToken(normalUser);

        mockMvc.perform(post("/api/invitations")
                        .param("companyId", existingCompanyId.toString())
                        .param("expirationInHours", "24")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Authorization denied."))
                .andExpect(jsonPath("$.status").value(403));
    }

    */
/**
     * Tests that providing an invalid (non-existent) company ID results in a 404 Not Found status.
     *//*

    @Test
    void testCreateBulkInvite_InvalidCompany() throws Exception {
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", "999") // Non-existent company ID
                        .param("expirationInHours", "24")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found."))
                .andExpect(jsonPath("$.status").value(404));
    }

    */
/**
     * Tests that providing invalid parameters (e.g., negative expiration time) results in a 400 Bad Request status.
     *//*

    @Test
    void testCreateBulkInvite_InvalidParameters() throws Exception {
        // Negative expirationInHours
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", existingCompanyId.toString())
                        .param("expirationInHours", "-5")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['createBulkInvite.expirationInHours']").value("Expiration time must be positive."));
    }
}
*/
