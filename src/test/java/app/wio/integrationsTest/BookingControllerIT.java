/*
// File: src/test/java/app/wio/integrationsTest/BookingControllerIT.java

package app.wio.integrationsTest;

import app.wio.dto.request.BookingRequestDto;
import app.wio.entity.*;
import app.wio.repository.*;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class BookingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long companyId;
    private Long floorId;
    private Long seatId;
    private Long inviteId;
    private Long userId;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Clear any existing persistence context
        entityManager.clear();

        // Initialize test data using repositories

        // Create Company
        Company company = new Company();
        company.setName("Test Company");
        company.setAddress("123 Test St");
        company = companyRepository.save(company);
        companyId = company.getId();

        // Create Floor
        Floor floor = new Floor();
        floor.setName("First Floor");
        floor.setFloorNumber(1);
        floor.setCompany(company);
        floor = floorRepository.save(floor);
        floorId = floor.getId();

        // Create Seat
        Seat seat = new Seat();
        seat.setSeatNumber("A1");
        seat.setXCoordinate(10.0);
        seat.setYCoordinate(20.0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFloor(floor);
        seat = seatRepository.save(seat);
        seatId = seat.getId();

        // Create Invite
        Invite invite = new Invite();
        invite.setToken("testInviteToken");
        invite.setExpiryDate(LocalDate.of(2099, 12, 31).atTime(23, 59, 59));
        invite.setCompany(company);
        invite.setJoinedCount(0);
        invite = inviteRepository.save(invite);
        inviteId = invite.getId();

        // Create User
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("hashedPassword"));
        user.setEnabled(true);
        user.setRole(UserRole.EMPLOYEE);
        user.setCompany(company);
        user.setInviteUsed(invite);
        user = userRepository.save(user);
        userId = user.getId();

        // Generate JWT token for the user
        userToken = testJwtTokenUtil.generateToken(user);

        // Flush and clear the persistence context to ensure entities are loaded fresh
        entityManager.flush();
        entityManager.clear();

        // Verify seat exists and is properly initialized
        Seat fetchedSeat = seatRepository.findById(seatId).orElseThrow();
        assertNotNull(fetchedSeat.getVersion(), "Seat version should not be null");

        // Verify user exists
        assertTrue(userRepository.findById(userId).isPresent(), "Test user should exist");
    }

    @Test
    void testCreateBooking() throws Exception {
        // Create the booking request
        BookingRequestDto req = new BookingRequestDto();
        req.setSeatId(seatId);
        req.setUserId(userId);
        req.setDate(LocalDate.now().plusDays(1));

        // Perform request with JWT token
        mockMvc.perform(post("/api/bookings/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.seatId").value(seatId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
*/
