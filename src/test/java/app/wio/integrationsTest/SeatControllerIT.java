// File: src/test/java/app/wio/integrationsTest/SeatControllerIT.java

package app.wio.integrationsTest;

import app.wio.entity.*;
import app.wio.repository.*;
import app.wio.security.TestJwtTokenUtil;
// Removed: import app.wio.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// Removed: import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // Activate 'test' profile
public class SeatControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestJwtTokenUtil jwtTokenUtil;

    private Long floorId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setName("Test Company");
        company.setAddress("123 Test St");
        company = companyRepository.save(company);

        Floor floor = new Floor();
        floor.setName("First Floor");
        floor.setFloorNumber(1);
        floor.setCompany(company);
        floor = floorRepository.save(floor);
        floorId = floor.getId();

        Seat seat1 = new Seat();
        seat1.setSeatNumber("A1");
        seat1.setXCoordinate(10.0);
        seat1.setYCoordinate(20.0);
        seat1.setStatus(SeatStatus.AVAILABLE);
        seat1.setFloor(floor);
        seatRepository.save(seat1);

        Seat seat2 = new Seat();
        seat2.setSeatNumber("A2");
        seat2.setXCoordinate(15.0);
        seat2.setYCoordinate(25.0);
        seat2.setStatus(SeatStatus.UNAVAILABLE);
        seat2.setFloor(floor);
        seatRepository.save(seat2);

        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("Admin123")); // Ensure the password is encoded
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);

        jwtToken = jwtTokenUtil.generateToken(adminUser);
    }

    @Test
    void testGetSeatsByFloorId() throws Exception {
        mockMvc.perform(get("/api/seats/floor/{floorId}", floorId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[1].seatNumber").value("A2"));
    }

    @Test
    void testCreateSeat() throws Exception {
        String newSeatJson = """
                {
                    "seatNumber": "A3",
                    "xCoordinate": 20.0,
                    "yCoordinate": 30.0,
                    "status": "AVAILABLE",
                    "floorId": %d
                }
                """.formatted(floorId);

        mockMvc.perform(post("/api/seats/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newSeatJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("A3"))
                .andExpect(jsonPath("$.xCoordinate").value(20.0))
                .andExpect(jsonPath("$.yCoordinate").value(30.0))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.floorId").value(floorId));
    }
}
