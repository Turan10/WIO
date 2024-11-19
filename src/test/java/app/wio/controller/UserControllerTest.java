package app.wio.controller;

import app.wio.dto.*;
import app.wio.entity.User;
import app.wio.entity.UserRole;
import app.wio.repository.UserRepository;
import app.wio.security.JwtAuthenticationEntryPoint;
import app.wio.security.JwtTokenProvider;
import app.wio.security.SecurityConfig;
import app.wio.security.TestJwtTokenUtil;
import app.wio.service.UserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, TestJwtTokenUtil.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenUtil testJwtTokenUtil;


    @Test
    public void testRegisterUser() throws Exception {
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPassword("password123");
        userDto.setCompanyId(1L); // Set to a valid value

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setName(userDto.getName());
        responseDto.setEmail(userDto.getEmail());

        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }


    @Test
    public void testGetUserById() throws Exception {
        Long userId = 1L;

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setName("John Doe");
        responseDto.setEmail("john.doe@example.com");

        when(userService.getUserById(userId)).thenReturn(responseDto);

        // Create Employee user
        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.EMPLOYEE);


        String token = testJwtTokenUtil.generateToken(user);

        // UserRepository Mock
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

}
