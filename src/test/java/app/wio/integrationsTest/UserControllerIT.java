package app.wio.integrationsTest;

import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserRoleDto;
import app.wio.entity.Company;
import app.wio.entity.OneTimeCode;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.OneTimeCodeRepository;
import app.wio.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OneTimeCodeRepository oneTimeCodeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private OneTimeCode validCode;

    @BeforeEach
    void setUp() {
        // Create a Company
        Company company = new Company();
        company.setName("UserTest Company");
        company.setAddress("Company Road");
        company = companyRepository.save(company);

        // Create an active OneTimeCode
        validCode = new OneTimeCode();
        validCode.setCompanyId(company.getId());
        validCode.setCode("TEST_CODE_123");
        validCode.setExpiryDate(LocalDateTime.now().plusHours(24));
        validCode.setUsedCount(0);
        oneTimeCodeRepository.save(validCode);
    }

    @Test
    void testRegisterAdmin() throws Exception {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Admin Person")
                .email("adminperson@example.com")
                .password("AdminPassword1")
                .role(UserRoleDto.ADMIN)
                .build();  // no oneTimeCode needed for ADMIN

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("adminperson@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        Optional<User> maybeUser = userRepository.findByEmail("adminperson@example.com");
        assertTrue(maybeUser.isPresent());
        assertEquals(UserRole.ADMIN, maybeUser.get().getRole());
    }

    @Test
    void testRegisterEmployeeWithValidOneTimeCode() throws Exception {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee User")
                .email("employeeuser@example.com")
                .password("Password123")
                .role(UserRoleDto.EMPLOYEE)
                .oneTimeCode("TEST_CODE_123")  // valid code
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("employeeuser@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        Optional<User> maybeUser = userRepository.findByEmail("employeeuser@example.com");
        assertTrue(maybeUser.isPresent());
        assertEquals(UserRole.EMPLOYEE, maybeUser.get().getRole());
        // Check that the code usage is incremented
        OneTimeCode updatedCode = oneTimeCodeRepository.findByCode("TEST_CODE_123").orElseThrow();
        assertEquals(1, updatedCode.getUsedCount());
    }

    @Test
    void testRegisterEmployeeMissingOneTimeCode() throws Exception {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee")
                .email("employee@example.com")
                .password("Password123")
                .role(UserRoleDto.EMPLOYEE)
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                // The top-level "message" is "Validation Failed"
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                // The detailed error is under $.errors.oneTimeCode
                .andExpect(jsonPath("$.errors.oneTimeCode")
                        .value("One-time code is required for EMPLOYEE registrations."));
    }

}
