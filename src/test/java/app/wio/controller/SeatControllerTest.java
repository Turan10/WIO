package app.wio.controller;

import app.wio.dto.SeatDto;
import app.wio.entity.Seat;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.UserRepository;
import app.wio.security.JwtAuthenticationEntryPoint;
import app.wio.security.JwtTokenProvider;
import app.wio.security.SecurityConfig;
import app.wio.security.TestJwtTokenUtil;
import app.wio.service.SeatService;
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

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@Import({SecurityConfig.class, TestJwtTokenUtil.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:application-test.properties")
public class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;


    @Test
    public void testCreateSeat() throws Exception {
        SeatDto seatDto = new SeatDto();
        seatDto.setSeatNumber("A1");
        seatDto.setXCoordinate(10.0);
        seatDto.setYCoordinate(20.0);
        seatDto.setFloorId(1L);

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber(seatDto.getSeatNumber());

        when(seatService.createSeat(any(Seat.class))).thenReturn(seat);

        // Admin user
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);


        String token = testJwtTokenUtil.generateToken(admin);

        // UserRepository Mock
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/seats/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seatDto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

}
