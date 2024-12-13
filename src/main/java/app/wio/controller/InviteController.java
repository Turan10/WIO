package app.wio.controller;

import app.wio.entity.Invite;
import app.wio.exception.CompanyNotFoundException;
import app.wio.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InviteController {

    private final InviteService inviteService;

    @Autowired
    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    // Create a bulk invite
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createBulkInvite(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "24") int expirationInHours) {

        Invite invite = inviteService.createInvite(companyId, expirationInHours);
        // Construct the invite link
        String inviteLink = "https://whosinoffice.com/invite/" + invite.getToken();

        Map<String, String> response = new HashMap<>();
        response.put("inviteLink", inviteLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Validate invite and return company info
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getInviteInfo(@PathVariable String token) {
        Invite invite = inviteService.getInviteByToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("companyId", invite.getCompany().getId());
        response.put("companyName", invite.getCompany().getName());

        return ResponseEntity.ok(response);
    }
}
