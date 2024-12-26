package app.wio.controller;

import app.wio.dto.request.BookingRequestDto;
import app.wio.dto.response.BookingResponseDto;
import app.wio.security.TestSecurityConfig;
import app.wio.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookingService bookingService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testCreateBookingSuccess() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setSeatId(1L);
        request.setUserId(2L);
        request.setDate(LocalDate.of(2024, 12, 18));

        BookingResponseDto response = new BookingResponseDto();
        response.setId(100L);
        response.setSeatId(1L);
        response.setUserId(2L);
        response.setDate(request.getDate());
        // The backend uses ACTIVE as default booking status
        response.setStatus("ACTIVE");

        Mockito.when(bookingService.createBooking(any(BookingRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testCreateBookingUnauthorized() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setSeatId(1L);
        request.setUserId(2L);
        request.setDate(LocalDate.of(2024, 12, 18));

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
