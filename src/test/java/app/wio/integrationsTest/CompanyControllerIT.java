package app.wio.integrationsTest;

import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CompanyControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testCreateCompany() throws Exception {
        CompanyCreationDto dto = new CompanyCreationDto();
        dto.setName("Novo Danmark");
        dto.setAddress("Birkevej 2");
        dto.setAdminName("Admin");
        dto.setAdminEmail("admin@novodk.com");
        dto.setAdminPassword("Secret123");

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Novo Danmark"))
                .andExpect(jsonPath("$.address").value("Birkevej 2"))

                // The response includes floorIds and userIds
                .andExpect(jsonPath("$.floorIds").isArray())
                .andExpect(jsonPath("$.floorIds").isEmpty())
                .andExpect(jsonPath("$.userIds").isArray())
                .andExpect(jsonPath("$.userIds.length()").value(1));

        // Verify DB insertion
        Optional<Company> maybeCompany = companyRepository.findByName("Novo Danmark");
        assertTrue(maybeCompany.isPresent());
        Company createdCompany = maybeCompany.get();
        // Check the newly created admin user
        Optional<User> maybeAdmin = userRepository.findByEmail("admin@novodk.com");
        assertTrue(maybeAdmin.isPresent());
        User adminUser = maybeAdmin.get();
        assertNotNull(adminUser.getId());
        // Admin should have role=ADMIN
        assertTrue(adminUser.getRole() == UserRole.ADMIN);
        // Company must match
        assertNotNull(adminUser.getCompany());
        assertTrue(adminUser.getCompany().getId().equals(createdCompany.getId()));
    }
}
