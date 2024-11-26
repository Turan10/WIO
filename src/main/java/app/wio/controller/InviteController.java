package app.wio.controller;

import app.wio.entity.Invite;
import app.wio.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;

    @Autowired
    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invite> createInvite(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "24") int expirationInHours) {

        Invite invite = inviteService.createInvite(companyId, expirationInHours);
        return ResponseEntity.status(HttpStatus.CREATED).body(invite);
    }

    @GetMapping("/{token}")
    public ResponseEntity<Invite> getInvite(@PathVariable String token) {
        Invite invite = inviteService.getInviteByToken(token);
        return ResponseEntity.ok(invite);
    }
}