package app.wio.controller;

import app.wio.security.TestConfig;
import app.wio.security.TestSecurityConfig;
import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserResponseDto;
import app.wio.entity.UserRole;
import app.wio.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TestConfig.class, TestSecurityConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterUser_Success() throws Exception {
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");
        userDto.setPassword("Password123");
        userDto.setInviteToken("invite-token");

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("John Doe");
        userResponseDto.setEmail("john@example.com");
        userResponseDto.setRole(UserRole.EMPLOYEE);
        userResponseDto.setToken(null);


        Mockito.when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(userResponseDto);


        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    public void testAuthenticateUser_Success() throws Exception {
        String email = "john@example.com";
        String password = "Password123";

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("John Doe");
        userResponseDto.setEmail(email);
        userResponseDto.setRole(UserRole.EMPLOYEE);
        userResponseDto.setToken("some-jwt-token");


        Mockito.when(userService.authenticateUser(eq(email), eq(password))).thenReturn(userResponseDto);


        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.token").value("some-jwt-token"));
    }
}