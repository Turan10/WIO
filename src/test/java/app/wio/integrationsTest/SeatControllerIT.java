package app.wio.integrationsTest;

import app.wio.dto.SeatDto;
import app.wio.entity.*;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorRepository;
import app.wio.repository.SeatRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private TestJwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Long floorId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create a Company
        Company company = new Company();
        company.setName("SeatTest Company");
        company.setAddress("1 Seat Street");
        company = companyRepository.save(company);

        // Create a Floor
        Floor floor = new Floor();
        floor.setName("Ground Floor");
        floor.setFloorNumber(1);
        floor.setCompany(company);
        floor = floorRepository.save(floor);
        this.floorId = floor.getId();

        // Create an Admin
        User admin = new User();
        admin.setName("SeatAdmin");
        admin.setEmail("seatadmin@example.com");
        admin.setPassword(passwordEncoder.encode("Admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setCompany(company);
        admin = userRepository.save(admin);

        // Generate token
        this.adminToken = jwtTokenUtil.generateToken(admin);

        // Also create 2 seats
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
    }

    @Test
    void testGetSeatsByFloorId() throws Exception {
        mockMvc.perform(get("/api/seats/floor/{floorId}", floorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[1].seatNumber").value("A2"));
    }

    @Test
    void testCreateSeat() throws Exception {
        SeatDto newSeat = new SeatDto();
        newSeat.setSeatNumber("B1");
        newSeat.setXCoordinate(50.0);
        newSeat.setYCoordinate(60.0);
        newSeat.setStatus(SeatStatus.AVAILABLE);
        newSeat.setFloorId(floorId);

        mockMvc.perform(post("/api/seats/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSeat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("B1"))
                .andExpect(jsonPath("$.xCoordinate").value(50.0))
                .andExpect(jsonPath("$.yCoordinate").value(60.0))
                .andExpect(jsonPath("$.floorId").value(floorId));

        List<Seat> seats = seatRepository.findByFloorId(floorId);
        assertTrue(seats.size() == 3);
    }

    @Test
    void testGetAvailableSeats() throws Exception {
        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", floorId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value("A1")); // only that seat is available
    }

    @Test
    void testCreateSeat_Unauthorized() throws Exception {
        SeatDto seatDto = new SeatDto();
        seatDto.setSeatNumber("Unauthorized");
        seatDto.setXCoordinate(10.0);
        seatDto.setYCoordinate(20.0);
        seatDto.setStatus(SeatStatus.AVAILABLE);
        seatDto.setFloorId(floorId);

        mockMvc.perform(post("/api/seats/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seatDto)))
                .andExpect(status().isUnauthorized());
    }
}
