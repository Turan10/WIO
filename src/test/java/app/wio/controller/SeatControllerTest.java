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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(SeatController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SeatService seatService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateSeat() throws Exception {
        SeatDto request = new SeatDto();
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

    @Test
    @WithMockUser
    void testGetAvailableSeats() throws Exception {
        SeatDto seat = new SeatDto();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setXCoordinate(10.0);
        seat.setYCoordinate(20.0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFloorId(1L);

        Mockito.when(seatService.getAvailableSeatsByFloorId(eq(1L)))
                .thenReturn(Collections.singletonList(seat));

        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[0].xCoordinate").value(10.0))
                .andExpect(jsonPath("$[0].yCoordinate").value(20.0))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void testGetAvailableSeatsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", "1"))
                .andExpect(status().isUnauthorized());
    }
}
