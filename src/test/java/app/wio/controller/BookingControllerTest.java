package app.wio.controller;

import app.wio.dto.BookingDto;
import app.wio.entity.Booking;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.UserRepository;
import app.wio.security.JwtAuthenticationEntryPoint;
import app.wio.security.JwtTokenProvider;
import app.wio.security.SecurityConfig;
import app.wio.security.TestJwtTokenUtil;
import app.wio.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, TestJwtTokenUtil.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:application-test.properties")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;


    @Test
    public void testCreateBooking() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setSeatId(1L);
        bookingDto.setUserId(1L);
        bookingDto.setDate(LocalDate.now());

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setDate(bookingDto.getDate());

        when(bookingService.createBooking(any(BookingDto.class))).thenReturn(booking);

        // User entity with EMPLOYEE role
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.EMPLOYEE);


        String token = testJwtTokenUtil.generateToken(user);

        // UserRepository Mock
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.date").value(bookingDto.getDate().toString()));
    }

    @Test
    public void testCancelBooking() throws Exception {
        Long bookingId = 1L;
        doNothing().when(bookingService).cancelBooking(bookingId);

        // User entity with EMPLOYEE role
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.EMPLOYEE);


        String token = testJwtTokenUtil.generateToken(user);

        // UserRepository Mock
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/bookings/cancel/{id}", bookingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetBookingsByUserId() throws Exception {
        Long userId = 1L;

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setDate(LocalDate.now());

        when(bookingService.getBookingsByUserId(userId))
                .thenReturn(Collections.singletonList(booking));

        // User entity with EMPLOYEE role
        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.EMPLOYEE);


        String token = testJwtTokenUtil.generateToken(user);

        // UserRepository Mock
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/bookings/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
