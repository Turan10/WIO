package app.wio.integrationsTest;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Company;
import app.wio.entity.Floor;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorRepository;
import app.wio.repository.UserRepository;
import app.wio.security.TestJwtTokenUtil;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestJwtTokenUtil jwtTokenUtil;

    private Long companyId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create a Company
        Company company = new Company();
        company.setName("FloorTest Company");
        company.setAddress("1234 Main");
        company = companyRepository.save(company);
        this.companyId = company.getId();

        // Create an Admin user
        User admin = new User();
        admin.setName("FloorAdmin");
        admin.setEmail("flooradmin@example.com");
        admin.setPassword(passwordEncoder.encode("Admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setCompany(company);
        admin = userRepository.save(admin);

        // Generate token
        this.adminToken = jwtTokenUtil.generateToken(admin);
    }

    @Test
    void testCreateFloor() throws Exception {
        FloorCreationDto dto = new FloorCreationDto();
        dto.setName("Test Floor");
        dto.setFloorNumber(1);
        dto.setCompanyId(companyId);

        mockMvc.perform(post("/api/floors/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Floor"))
                .andExpect(jsonPath("$.floorNumber").value(1))
                .andExpect(jsonPath("$.companyId").value(companyId))
                .andExpect(jsonPath("$.seatIds").isArray())
                .andExpect(jsonPath("$.seatIds.length()").value(0));

        List<Floor> allFloors = floorRepository.findAll();
        assertTrue(allFloors.size() > 0);
    }

    @Test
    void testGetFloorsByCompanyId() throws Exception {
        // Create a Floor
        Floor floor = new Floor();
        floor.setName("Floor A");
        floor.setFloorNumber(2);
        floor.setCompany(companyRepository.findById(companyId).orElseThrow());
        floorRepository.save(floor);

        mockMvc.perform(get("/api/floors/company/" + companyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Floor A"))
                .andExpect(jsonPath("$[0].floorNumber").value(2));

        List<Floor> floors = floorRepository.findByCompanyId(companyId);
        assertEquals(1, floors.size());
    }

    @Test
    void testCreateFloor_Unauthorized() throws Exception {
        FloorCreationDto dto = new FloorCreationDto("Unauthorized Floor", 99, companyId);

        // No token → 401
        mockMvc.perform(post("/api/floors/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
