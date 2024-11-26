// src/test/java/app/wio/controller/BookingControllerTest.java

package app.wio.controller;

import app.wio.security.TestConfig;
import app.wio.dto.BookingDto;
import app.wio.entity.Booking;
import app.wio.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import csrf()
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(TestConfig.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithAnonymousUser
    public void testCreateBooking_Unauthorized() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, 1L, LocalDate.now());

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    public void testCreateBooking_Success() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, 1L, LocalDate.now());
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setDate(LocalDate.now());
        // Set other properties as needed

        Mockito.when(bookingService.createBooking(any(BookingDto.class))).thenReturn(booking);

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}