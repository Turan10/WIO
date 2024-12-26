/*
package app.wio.controller;

import app.wio.dto.SeatDto;
import app.wio.entity.SeatStatus;
import app.wio.security.TestSecurityConfig;
import app.wio.service.SeatService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

*/
/**
 * SeatControllerTest contains unit tests for the SeatController.
 *//*

@WebMvcTest(SeatController.class)
@ActiveProfiles("test") // Activate test profile
@Import(TestSecurityConfig.class) // Import TestSecurityConfig
class SeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SeatService seatService;

    @Autowired
    ObjectMapper objectMapper;

    */
/**
     * Tests successful creation of a seat by an authenticated ADMIN user.
     *//*

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateSeat() throws Exception {
        // Given
        SeatDto request = new SeatDto();
        request.setId(null);
        request.setSeatNumber("A1");
        request.setXCoordinate(10.0);
        request.setYCoordinate(20.0);
        request.setStatus(SeatStatus.AVAILABLE);
        request.setFloorId(1L);

        SeatDto response = new SeatDto();
        response.setId(1L);
        response.setSeatNumber("A1");
        response.setXCoordinate(10.0);
        response.setYCoordinate(20.0);
        response.setStatus(SeatStatus.AVAILABLE);
        response.setFloorId(1L);

        Mockito.when(seatService.createSeat(any(SeatDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/seats/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.xCoordinate").value(10.0))
                .andExpect(jsonPath("$.yCoordinate").value(20.0))
                .andExpect(jsonPath("$.floorId").value(1));
    }

    */
/**
     * Tests fetching available seats by floor ID and date as an authenticated user.
     *//*

    @Test
    @WithMockUser
    void testGetAvailableSeats() throws Exception {
        // Given
        SeatDto seat = new SeatDto();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setXCoordinate(10.0);
        seat.setYCoordinate(20.0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFloorId(1L);

        Mockito.when(seatService.getAvailableSeatsByFloorIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(seat));

        // When & Then
        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", "1")
                        .param("date", "2024-12-18")) // Fixed date for consistency
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[0].xCoordinate").value(10.0))
                .andExpect(jsonPath("$[0].yCoordinate").value(20.0));
    }

    */
/**
     * Tests that fetching available seats without authentication receives a 401 Unauthorized status.
     *//*

    @Test
    void testGetAvailableSeatsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", "1")
                        .param("date", "2024-12-18"))
                .andExpect(status().isUnauthorized());
    }
}
*/
