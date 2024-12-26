// File: src/test/java/app/wio/integrationsTest/FloorControllerIT.java

package app.wio.integrationsTest;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorRepository;
import app.wio.repository.UserRepository;
import app.wio.security.TestJwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class FloorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private Long existingCompanyId;
    private String adminToken;

    @BeforeEach
    void setUp() {

        entityManager.clear();
        Company company = new Company();
        company.setName("Test Company");
        company.setAddress("123 Test St");

        company = companyRepository.save(company);
        existingCompanyId = company.getId();

        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setEnabled(true);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setCompany(company);

        adminUser = userRepository.save(adminUser);

        adminToken = testJwtTokenUtil.generateToken(adminUser);

        entityManager.flush();
        entityManager.clear();

        assertNotNull(existingCompanyId, "Created company should have an ID");
        assertTrue(companyRepository.findById(existingCompanyId).isPresent(), "Test company should exist");
    }

    @Test
    void testCreateFloor() throws Exception {
        FloorCreationDto dto = new FloorCreationDto("Second Floor", 2, existingCompanyId);

        mockMvc.perform(post("/api/floors/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Second Floor"))
                .andExpect(jsonPath("$.floorNumber").value(2))
                .andExpect(jsonPath("$.companyId").value(existingCompanyId))
                .andExpect(jsonPath("$.seatIds").isArray())
                .andExpect(jsonPath("$.seatIds.length()").value(0));
    }

    @Test
    void testGetFloorsByCompanyId() throws Exception {
        FloorCreationDto dto = new FloorCreationDto("First Floor", 1, existingCompanyId);

        mockMvc.perform(post("/api/floors/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("First Floor"))
                .andExpect(jsonPath("$.floorNumber").value(1))
                .andExpect(jsonPath("$.companyId").value(existingCompanyId))
                .andExpect(jsonPath("$.seatIds").isArray())
                .andExpect(jsonPath("$.seatIds.length()").value(0));

        mockMvc.perform(get("/api/floors/company/" + existingCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("First Floor"))
                .andExpect(jsonPath("$[0].floorNumber").value(1))
                .andExpect(jsonPath("$[0].companyId").value(existingCompanyId))
                .andExpect(jsonPath("$[0].seatIds").isArray())
                .andExpect(jsonPath("$[0].seatIds.length()").value(0));
    }

    @Test
    void testCreateFloorValidationFailure() throws Exception {
        FloorCreationDto dto = new FloorCreationDto("Invalid Floor", 0, null);

        mockMvc.perform(post("/api/floors/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.floorNumber").value("Floor number must be at least 1."))
                .andExpect(jsonPath("$.errors.companyId").value("Company ID is required."));
    }

    @Test
    void testGetFloorsByCompanyId_NoFloors() throws Exception {
        mockMvc.perform(get("/api/floors/company/" + existingCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
