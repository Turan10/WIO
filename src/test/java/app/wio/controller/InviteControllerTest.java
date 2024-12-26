/*
package app.wio.controller;

import app.wio.dto.response.InviteResponseDto;
import app.wio.entity.Invite;
import app.wio.mapper.InviteMapper;
import app.wio.security.TestSecurityConfig;
import app.wio.service.InviteService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

*/
/**
 * InviteControllerTest contains unit tests for the InviteController.
 *//*

@WebMvcTest(InviteController.class)
@ActiveProfiles("test") // Activate test profile
@Import(TestSecurityConfig.class) // Import TestSecurityConfig
class InviteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    InviteService inviteService;

    @MockBean
    InviteMapper inviteMapper;

    */
/**
     * Tests successful creation of a bulk invite by an authenticated ADMIN user.
     *//*

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBulkInvite() throws Exception {
        // Given
        Invite invite = new Invite();
        invite.setId(1L);
        invite.setToken("test-token");
        invite.setExpiryDate(LocalDateTime.of(2024, 12, 18, 0, 0));

        InviteResponseDto dto = new InviteResponseDto();
        dto.setId(1L);
        dto.setCompanyId(1L);
        dto.setInviteLink("https://whosinoffice.com/invite/test-token");
        dto.setCompanyName("Test Company");
        dto.setExpiryDate(invite.getExpiryDate());
        dto.setJoinedCount(0);
        dto.setJoinedUsers(Collections.emptyList());

        Mockito.when(inviteService.createInvite(anyLong(), anyInt())).thenReturn(invite);
        Mockito.when(inviteMapper.toDto(invite)).thenReturn(dto);

        // When & Then
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", "1")
                        .param("expirationInHours", "24")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.inviteLink").value("https://whosinoffice.com/invite/test-token"))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.joinedCount").value(0))
                .andExpect(jsonPath("$.joinedUsers").isEmpty());
    }

    */
/**
     * Tests that creating a bulk invite without authentication receives a 401 Unauthorized status.
     *//*

    @Test
    void testCreateBulkInviteUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/invitations")
                        .param("companyId", "1")
                        .param("expirationInHours", "24")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
*/
