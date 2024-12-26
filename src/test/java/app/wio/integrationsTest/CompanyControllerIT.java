package app.wio.integrationsTest;

import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.repository.CompanyRepository;
import app.wio.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class CompanyControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        // No need to create an admin or token upfront since we're now testing unauthenticated company creation
    }

    @Test
    void testCreateCompany() throws Exception {
        CompanyCreationDto companyDto = new CompanyCreationDto();
        companyDto.setName("Another Company");
        companyDto.setAddress("456 Avenue");
        companyDto.setFloorCount(2);
        companyDto.setFloorNames(Arrays.asList("Second Floor", "Third Floor"));
        companyDto.setAdminName("Another Admin");
        companyDto.setAdminEmail("anotheradmin@example.com");
        companyDto.setAdminPassword("secret123");

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Another Company"))
                .andExpect(jsonPath("$.address").value("456 Avenue"))
                .andExpect(jsonPath("$.floorIds.length()").value(2));

        // Verify the company and admin user are created in DB
        entityManager.flush();
        entityManager.clear();

        assertTrue(companyRepository.findByName("Another Company").isPresent());
        assertNotNull(userRepository.findByEmail("anotheradmin@example.com").orElse(null));
    }
}
