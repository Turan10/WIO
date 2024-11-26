package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Floor;
import app.wio.security.TestConfig;
import app.wio.service.FloorService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FloorController.class)
@Import(TestConfig.class)
public class FloorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FloorService floorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateFloor_Success() throws Exception {
        FloorCreationDto floorDto = new FloorCreationDto("First Floor", 1, 1L);
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setName("First Floor");


        Mockito.when(floorService.createFloor(any(FloorCreationDto.class))).thenReturn(floor);

        mockMvc.perform(post("/api/floors/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(floorDto))
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("First Floor"));
    }

    @Test
    @WithAnonymousUser
    public void testCreateFloor_Unauthorized() throws Exception {
        FloorCreationDto floorDto = new FloorCreationDto("First Floor", 1, 1L);

        mockMvc.perform(post("/api/floors/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(floorDto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void testGetFloorsByCompanyId() throws Exception {
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setName("First Floor");

        Mockito.when(floorService.getFloorsByCompanyId(1L)).thenReturn(Collections.singletonList(floor));

        mockMvc.perform(get("/api/floors/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("First Floor"));
    }
}