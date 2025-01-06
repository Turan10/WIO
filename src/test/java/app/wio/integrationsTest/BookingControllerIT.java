package app.wio.integrationsTest;

import app.wio.dto.request.BookingRequestDto;
import app.wio.entity.*;
import app.wio.repository.BookingRepository;
import app.wio.repository.SeatRepository;
import app.wio.repository.UserRepository;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorRepository;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestJwtTokenUtil jwtTokenUtil;

    private String employeeToken;
    private Long seatId;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        // Create a Company
        Company company = new Company();
        company.setName("Exam");
        company.setAddress("123 Testvej");
        company = companyRepository.save(company);

        // Create a Floor
        Floor floor = new Floor();
        floor.setName("Main Floor");
        floor.setFloorNumber(1);
        floor.setCompany(company);
        floor = floorRepository.save(floor);

        // Create a Seat
        Seat seat = new Seat();
        seat.setSeatNumber("A1");
        seat.setXCoordinate(10.0);
        seat.setYCoordinate(20.0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFloor(floor);
        seat = seatRepository.save(seat);
        this.seatId = seat.getId();

        // Create an EMPLOYEE User
        User employee = new User();
        employee.setName("Employee User");
        employee.setEmail("employee@example.com");
        employee.setPassword(passwordEncoder.encode("Password123"));
        employee.setRole(UserRole.EMPLOYEE);
        employee.setEnabled(true);
        employee.setCompany(company);
        employee = userRepository.save(employee);
        this.employeeId = employee.getId();

        // Generate JWT for EMPLOYEE
        this.employeeToken = jwtTokenUtil.generateToken(employee);
    }

    @Test
    void testCreateBooking() throws Exception {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setSeatId(seatId);
        dto.setUserId(employeeId);
        dto.setDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/bookings/create")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.seatId").value(seatId))
                .andExpect(jsonPath("$.userId").value(employeeId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify that it was saved to DB
        assertTrue(bookingRepository.findAll().size() > 0);
    }

    @Test
    void testCreateBooking_Unauthorized() throws Exception {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setSeatId(seatId);
        dto.setUserId(employeeId);
        dto.setDate(LocalDate.now().plusDays(1));

        // No token → Expect 401
        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
