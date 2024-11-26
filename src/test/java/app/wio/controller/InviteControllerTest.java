package app.wio.controller;

import app.wio.security.TestConfig;
import app.wio.entity.Invite;
import app.wio.service.InviteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import csrf()
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InviteController.class)
@Import(TestConfig.class)
public class InviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InviteService inviteService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateInvite_Success() throws Exception {
        Invite invite = new Invite();
        invite.setId(1L);
        invite.setToken("test-token");
        invite.setExpiryDate(LocalDateTime.now().plusHours(24));


        Mockito.when(inviteService.createInvite(anyLong(), anyInt())).thenReturn(invite);

        mockMvc.perform(post("/api/invites/create")
                        .param("companyId", "1")
                        .param("expirationInHours", "24")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    @WithAnonymousUser
    public void testCreateInvite_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/invites/create")
                        .param("companyId", "1")
                        .param("expirationInHours", "24")
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void testGetInvite() throws Exception {
        Invite invite = new Invite();
        invite.setId(1L);
        invite.setToken("test-token");

        Mockito.when(inviteService.getInviteByToken("test-token")).thenReturn(invite);

        mockMvc.perform(get("/api/invites/test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.token").value("test-token"));
    }
}