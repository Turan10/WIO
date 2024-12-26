package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.dto.response.FloorDto;
import app.wio.entity.Company;
import app.wio.entity.Floor;
import app.wio.service.FloorService;
import app.wio.service.SeatService;
import app.wio.mapper.FloorMapper; // Added FloorMapper
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FloorController.class)
public class FloorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FloorService floorService;

    @MockBean
    private SeatService seatService;

    @MockBean
    private FloorMapper floorMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private FloorCreationDto floorCreationDto;
    private Floor floor;

    @BeforeEach
    void setUp() {
        floorCreationDto = new FloorCreationDto();
        floorCreationDto.setName("First Floor");
        floorCreationDto.setFloorNumber(1);
        floorCreationDto.setCompanyId(1L);

        floor = new Floor();
        floor.setId(100L);
        floor.setName("First Floor");
        floor.setFloorNumber(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateFloor() throws Exception {
        FloorDto responseDto = new FloorDto();
        responseDto.setId(1L);
        responseDto.setName("First Floor");
        responseDto.setFloorNumber(1);
        responseDto.setCompanyId(1L);
        responseDto.setSeatIds(Collections.emptyList());

        when(floorService.createFloor(any(FloorCreationDto.class))).thenReturn(new Floor());
        when(floorMapper.toDto(any(Floor.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/floors/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"First Floor\",\"floorNumber\":1,\"companyId\":1}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("First Floor"))
                .andExpect(jsonPath("$.floorNumber").value(1))
                .andExpect(jsonPath("$.companyId").value(1));
    }
}
