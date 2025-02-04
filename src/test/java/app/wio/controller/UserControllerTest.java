package app.wio.controller;

import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserRoleDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.entity.UserRole;
import app.wio.security.TestSecurityConfig;
import app.wio.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testRegisterUser() throws Exception {

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .name("John Andersen")
                .email("john@example.com")
                .password("Password123")
                .role(UserRoleDto.EMPLOYEE)
                .oneTimeCode("SOME_VALID_CODE")
                .build();

        UserResponseDto response = new UserResponseDto();
        response.setId(1L);
        response.setName("John Andersen");
        response.setEmail("john@example.com");
        response.setRole(UserRole.EMPLOYEE);
        response.setToken("MOCKED_JWT_TOKEN");

        Mockito.when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.token").value("MOCKED_JWT_TOKEN"));
    }
}
