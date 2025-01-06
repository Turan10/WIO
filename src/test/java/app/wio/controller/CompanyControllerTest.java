package app.wio.controller;

import app.wio.dto.CompanyCreationDto;
import app.wio.dto.response.CompanyDto;
import app.wio.security.TestSecurityConfig;
import app.wio.service.CompanyService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateCompany() throws Exception {
        CompanyCreationDto request = new CompanyCreationDto();
        request.setName("Cph Business");
        request.setAddress("Firskovvej 2");
        request.setAdminName("Admin User");
        request.setAdminEmail("admin@cphbusiness.dk");
        request.setAdminPassword("password123");

        CompanyDto response = new CompanyDto();
        response.setId(10L);
        response.setName("Cph Business");
        response.setAddress("Firskovvej 2");
        response.setFloorIds(Collections.emptyList());
        response.setUserIds(Collections.singletonList(500L));

        Mockito.when(companyService.createCompany(any(CompanyCreationDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Cph Business"))
                .andExpect(jsonPath("$.address").value("Firskovvej 2"))
                .andExpect(jsonPath("$.floorIds.length()").value(0))
                .andExpect(jsonPath("$.userIds.length()").value(1));
    }
}
