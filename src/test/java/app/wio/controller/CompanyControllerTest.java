package app.wio.controller;

import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.UserRepository;
import app.wio.security.JwtAuthenticationEntryPoint;
import app.wio.security.JwtTokenProvider;
import app.wio.security.SecurityConfig;
import app.wio.security.TestJwtTokenUtil;
import app.wio.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, TestJwtTokenUtil.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:application-test.properties")
public class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;

    // Removed the @MockBean for JwtTokenProvider to use the actual bean

    @Test
    public void testCreateCompany() throws Exception {
        CompanyCreationDto companyDto = new CompanyCreationDto();
        companyDto.setName("Test Company");
        companyDto.setAddress("123 Test St");
        companyDto.setFloorCount(2);
        companyDto.setFloorNames(Arrays.asList("First Floor", "Second Floor"));

        Company company = new Company();
        company.setId(1L);
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());
        company.setFloors(Collections.emptyList());

        when(companyService.createCompany(any(Company.class))).thenReturn(company);

        // Create an admin User entity
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        // Generate JWT token
        String token = testJwtTokenUtil.generateToken(admin);

        // Mock UserRepository behavior
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Company"));
    }

    // Include other test methods with similar adjustments
}
