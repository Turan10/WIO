package app.wio.controller;

import app.wio.dto.request.ShareRequestDto;
import app.wio.dto.response.ShareResponseDto;
import app.wio.security.CustomUserDetails;
import app.wio.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    @Autowired
    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShareResponseDto> createShare(
            @RequestBody ShareRequestDto requestDto,
            Principal principal
    ) {
        var userDetails = (CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal();
        Long senderId = userDetails.getId();

        ShareResponseDto result = shareService.createShare(senderId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShareResponseDto>> getInbox(Principal principal) {
        var userDetails = (CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal();
        Long recipientId = userDetails.getId();

        List<ShareResponseDto> shares = shareService.getSharesForRecipient(recipientId);
        return ResponseEntity.ok(shares);
    }


    @PatchMapping("/{shareId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShareResponseDto> markShareAsRead(
            @PathVariable Long shareId,
            Principal principal
    ) {
        var userDetails = (CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal();
        Long recipientId = userDetails.getId();

        ShareResponseDto updated = shareService.markShareAsRead(shareId, recipientId);
        return ResponseEntity.ok(updated);
    }


    @PatchMapping("/{shareId}/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShareResponseDto> markShareAsUnread(
            @PathVariable Long shareId,
            Principal principal
    ) {
        var userDetails = (CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal();
        Long recipientId = userDetails.getId();

        ShareResponseDto updated = shareService.markShareAsUnread(shareId, recipientId);
        return ResponseEntity.ok(updated);
    }
}
