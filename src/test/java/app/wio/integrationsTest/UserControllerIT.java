/*
// File: src/test/java/app/wio/integrationsTest/UserControllerIT.java

package app.wio.integrationsTest;

import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserRoleDto;
import app.wio.entity.Company;
import app.wio.entity.Invite;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.InviteRepository;
import app.wio.repository.UserRepository;
import app.wio.security.TestJwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;

    private Long existingCompanyId;
    private String adminToken;
    private Invite validInvite;

    private String generateUniqueEmail() {
        return "employee_" + UUID.randomUUID().toString() + "@example.com";
    }

    private String generateUniqueInviteToken() {
        return "validInviteToken_" + UUID.randomUUID().toString();
    }

    @BeforeEach
    void setUp() {
        entityManager.clear();

        // Create a Company
        Company company = new Company();
        company.setName("Test Company");
        company.setAddress("123 Test St");
        company = companyRepository.save(company);
        existingCompanyId = company.getId();

        // Create an ADMIN user
        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin_" + UUID.randomUUID().toString() + "@example.com");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123"));
        adminUser.setEnabled(true); // Admin enabled
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setCompany(company);
        adminUser = userRepository.save(adminUser);
        System.out.println("Created ADMIN user with email: " + adminUser.getEmail());

        adminToken = testJwtTokenUtil.generateToken(adminUser);
        System.out.println("Generated JWT token for ADMIN user.");

        // Create an Invite for Employees
        Invite invite = new Invite();
        invite.setCompany(company);
        invite.setExpiryDate(LocalDateTime.now().plusDays(1));
        invite.setToken(generateUniqueInviteToken());
        invite.setJoinedCount(0);
        invite.setJoinedUsers(new java.util.ArrayList<>());
        invite = inviteRepository.save(invite);
        validInvite = invite;
        System.out.println("Created Invite with token: " + validInvite.getToken());

        entityManager.flush();
        entityManager.clear();

        assertNotNull(existingCompanyId, "Created company should have an ID");
        assertTrue(companyRepository.findById(existingCompanyId).isPresent(), "Test company should exist");
        assertNotNull(adminUser.getId(), "Created admin user should have an ID");
        assertTrue(userRepository.findById(adminUser.getId()).isPresent(), "Admin user should exist");

        Optional<Invite> optionalInvite = inviteRepository.findByToken(validInvite.getToken());
        assertTrue(optionalInvite.isPresent(), "Invite token should exist");
        Invite savedInvite = optionalInvite.get();
        assertEquals(company.getId(), savedInvite.getCompany().getId(), "Invite should be associated with the correct company");
    }

    @Test
    void testRegisterAdminUser() throws Exception {
        String uniqueEmail = "newadmin_" + UUID.randomUUID().toString() + "@example.com";
        System.out.println("Registering ADMIN user with email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("New Admin")
                .email(uniqueEmail)
                .password("AdminPass123")
                .role(UserRoleDto.ADMIN)
                .inviteToken(null)
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        Optional<User> adminOpt = userRepository.findByEmail(uniqueEmail);
        assertTrue(adminOpt.isPresent(), "New ADMIN should be present in the repository");
        User admin = adminOpt.get();
        assertEquals(UserRole.ADMIN, admin.getRole(), "User role should be ADMIN");
        assertNull(admin.getInviteUsed(), "ADMIN should not have an invite associated");
    }

    @Test
    void testRegisterEmployeeUser_WithValidInviteToken() throws Exception {
        String uniqueEmail = generateUniqueEmail();
        System.out.println("Registering EMPLOYEE user with email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee User")
                .email(uniqueEmail)
                .password("Employee123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(validInvite.getToken())
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        Optional<User> employeeOpt = userRepository.findByEmail(uniqueEmail);
        assertTrue(employeeOpt.isPresent(), "EMPLOYEE should be present in the repository");
        User employee = employeeOpt.get();
        assertEquals(UserRole.EMPLOYEE, employee.getRole(), "User role should be EMPLOYEE");
        assertNotNull(employee.getInviteUsed(), "EMPLOYEE should have an invite associated");
        assertEquals(validInvite.getId(), employee.getInviteUsed().getId(), "Invite ID should match");

        Invite updatedInvite = inviteRepository.findById(validInvite.getId()).orElse(null);
        assertNotNull(updatedInvite, "Invite should exist");
        assertEquals(1, updatedInvite.getJoinedCount(), "Invite joined count should be incremented");
        assertTrue(updatedInvite.getJoinedUsers().contains(employee), "Invite's joinedUsers should contain the new EMPLOYEE");
    }

    @Test
    void testRegisterEmployeeUser_WithoutInviteToken() throws Exception {
        String uniqueEmail = "employeewithouttoken_" + UUID.randomUUID().toString() + "@example.com";
        System.out.println("Attempting to register EMPLOYEE without invite token with email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee Without Token")
                .email(uniqueEmail)
                .password("Employee123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(null)
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.inviteToken").value("Invite token is required for EMPLOYEE registrations."));
    }

    @Test
    void testRegisterEmployeeUser_WithInvalidInviteToken() throws Exception {
        String uniqueEmail = "employeeinvalidtoken_" + UUID.randomUUID().toString() + "@example.com";
        System.out.println("Attempting to register EMPLOYEE with invalid invite token using email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee Invalid Token")
                .email(uniqueEmail)
                .password("Employee123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken("invalidInviteToken_" + UUID.randomUUID().toString())
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid invite token."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        String uniqueEmail = "john_" + UUID.randomUUID().toString() + "@example.com";
        System.out.println("Registering EMPLOYEE user with email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("John Doe")
                .email(uniqueEmail)
                .password("Password123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(validInvite.getToken())
                .build();

        // First registration
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Attempt duplicate registration
        System.out.println("Attempting to register duplicate EMPLOYEE user with email: " + uniqueEmail);
        UserRegistrationDto dtoDuplicate = UserRegistrationDto.builder()
                .name("John Duplicate")
                .email(uniqueEmail)
                .password("Password123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(validInvite.getToken())
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoDuplicate)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists."))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testRegisterUser_InvalidData() throws Exception {
        // Missing required fields or invalid data
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("") // Invalid name
                .email("invalid-email") // Invalid email
                .password("123") // Too short and doesn't meet pattern
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(validInvite.getToken())
                .build();

        System.out.println("Attempting to register EMPLOYEE with invalid data.");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required."))
                .andExpect(jsonPath("$.errors.email").value("Invalid email format."))
                // Use anyOf for the password error since multiple constraints fail
                .andExpect(jsonPath("$.errors.password").value(Matchers.anyOf(
                        Matchers.is("Password must be at least 6 characters long."),
                        Matchers.is("Password must contain at least one uppercase letter, one lowercase letter, and one digit.")
                )));
    }

    @Test
    void testAdminRetrieveInviteDetails_WithJoinedUsers() throws Exception {
        String uniqueEmail = generateUniqueEmail();
        System.out.println("Registering EMPLOYEE user with email: " + uniqueEmail);

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("Employee User")
                .email(uniqueEmail)
                .password("Employee123")
                .role(UserRoleDto.EMPLOYEE)
                .inviteToken(validInvite.getToken())
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        System.out.println("ADMIN retrieving invite details for invite ID: " + validInvite.getId());

        mockMvc.perform(get("/api/users/invites/" + validInvite.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validInvite.getId()))
                .andExpect(jsonPath("$.token").value(validInvite.getToken()))
                .andExpect(jsonPath("$.inviteLink").value("https://yourdomain.com/invite/" + validInvite.getToken()))
                .andExpect(jsonPath("$.companyId").value(existingCompanyId))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.expiryDate").exists())
                .andExpect(jsonPath("$.joinedCount").value(1))
                .andExpect(jsonPath("$.joinedUsers").isArray())
                .andExpect(jsonPath("$.joinedUsers[0].email").value(uniqueEmail))
                .andExpect(jsonPath("$.joinedUsers[0].name").value("Employee User"))
                .andExpect(jsonPath("$.joinedUsers[0].role").value("EMPLOYEE"));
    }

    @Test
    void testAdminRetrieveInviteDetails_NoJoinedUsers() throws Exception {
        System.out.println("ADMIN retrieving invite details for invite ID with no joined users: " + validInvite.getId());

        mockMvc.perform(get("/api/users/invites/" + validInvite.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validInvite.getId()))
                .andExpect(jsonPath("$.token").value(validInvite.getToken()))
                // Now we have corrected code to set inviteLink, companyId, and companyName
                .andExpect(jsonPath("$.inviteLink").value("https://yourdomain.com/invite/" + validInvite.getToken()))
                .andExpect(jsonPath("$.companyId").value(existingCompanyId))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.expiryDate").exists())
                .andExpect(jsonPath("$.joinedCount").value(0))
                .andExpect(jsonPath("$.joinedUsers").isArray())
                .andExpect(jsonPath("$.joinedUsers").isEmpty());
    }
}
*/
