package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Floor;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.UserRepository;
import app.wio.security.JwtAuthenticationEntryPoint;
import app.wio.security.JwtTokenProvider;
import app.wio.security.SecurityConfig;
import app.wio.security.TestJwtTokenUtil;
import app.wio.service.CompanyService;
import app.wio.service.FloorService;
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

@WebMvcTest(FloorController.class)
@Import({SecurityConfig.class, TestJwtTokenUtil.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:application-test.properties")
public class FloorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FloorService floorService;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;


    @Test
    public void testCreateFloor() throws Exception {
        FloorCreationDto floorDto = new FloorCreationDto();
        floorDto.setName("Third Floor");
        floorDto.setFloorNumber(3);
        floorDto.setCompanyId(1L);

        Floor floor = new Floor();
        floor.setId(1L);
        floor.setName(floorDto.getName());
        floor.setFloorNumber(floorDto.getFloorNumber());

        when(companyService.getCompanyById(floorDto.getCompanyId())).thenReturn(null); // Adjust as needed
        when(floorService.getFloorByNameAndCompanyId(floorDto.getName(), floorDto.getCompanyId())).thenReturn(null);
        when(floorService.createFloor(any(Floor.class))).thenReturn(floor);

        // Admin user
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);


        String token = testJwtTokenUtil.generateToken(admin);

        // UserRepository Mock
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/floors/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(floorDto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Third Floor"));
    }


}
