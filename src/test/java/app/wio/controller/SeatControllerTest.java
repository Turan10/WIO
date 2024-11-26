package app.wio.controller;

import app.wio.security.TestConfig;
import app.wio.dto.SeatDto; // Import SeatDto
import app.wio.entity.Seat;
import app.wio.entity.SeatStatus;
import app.wio.service.SeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import csrf()
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@Import(TestConfig.class)
public class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateSeat_Success() throws Exception {
        SeatDto seatDto = new SeatDto();
        seatDto.setSeatNumber("A1");
        seatDto.setStatus(SeatStatus.AVAILABLE);
        seatDto.setXCoordinate(10.0);
        seatDto.setYCoordinate(20.0);
        seatDto.setFloorId(1L);


        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setXCoordinate(10.0);
        seat.setYCoordinate(20.0);


        Mockito.when(seatService.createSeat(any(SeatDto.class))).thenReturn(seat);

        mockMvc.perform(post("/api/seats/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(seatDto))
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    @WithAnonymousUser
    public void testCreateSeat_Unauthorized() throws Exception {
        SeatDto seatDto = new SeatDto();
        seatDto.setSeatNumber("A1");
        seatDto.setStatus(SeatStatus.AVAILABLE);
        seatDto.setXCoordinate(10.0);
        seatDto.setYCoordinate(20.0);
        seatDto.setFloorId(1L);


        mockMvc.perform(post("/api/seats/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(seatDto))
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void testGetAvailableSeats() throws Exception {
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setStatus(SeatStatus.AVAILABLE);

        Mockito.when(seatService.getAvailableSeatsByFloorIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(seat));

        mockMvc.perform(get("/api/seats/available")
                        .param("floorId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value("A1"));

    }
}
