package app.wio.controller;

import app.wio.controller.CompanyController;
import app.wio.security.TestConfig;
import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import csrf()
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import(TestConfig.class)
public class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateCompany_Success() throws Exception {
        CompanyCreationDto companyDto = new CompanyCreationDto(
                "Test Company", "123 Street", 3, Collections.singletonList("Floor 1"));
        Company company = new Company();
        company.setId(1L);
        company.setName("Test Company");


        Mockito.when(companyService.createCompany(any(CompanyCreationDto.class))).thenReturn(company);

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Company"));
    }

    @Test
    @WithAnonymousUser
    public void testCreateCompany_Unauthorized() throws Exception {
        CompanyCreationDto companyDto = new CompanyCreationDto(
                "Test Company", "123 Street", 3, Collections.singletonList("Floor 1"));

        mockMvc.perform(post("/api/companies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void testGetCompanyById() throws Exception {
        Company company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        Mockito.when(companyService.getCompanyById(1L)).thenReturn(company);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUsersByCompanyId() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        Mockito.when(companyService.getUsersByCompanyId(1L)).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/companies/1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @WithAnonymousUser
    public void testGetUsersByCompanyId_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/companies/1/users"))
                .andExpect(status().isUnauthorized());
    }
}