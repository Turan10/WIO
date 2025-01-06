package app.wio.controller;

import app.wio.dto.request.ShareRequestDto;
import app.wio.dto.response.ShareResponseDto;
import app.wio.security.CustomUserDetails;
import app.wio.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shares")
@Tag(name = "Share API", description = "Endpoints for sharing booking info among employees")
public class ShareController {

    private final ShareService shareService;

    @Autowired
    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @Operation(summary = "Create a share", description = "Shares specific bookings with another user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Share created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authenticated")
    })
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

    @Operation(summary = "Get inbox shares", description = "Retrieves all share messages for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of shares retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authenticated")
    })
    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShareResponseDto>> getInbox(Principal principal) {
        var userDetails = (CustomUserDetails)
                ((org.springframework.security.core.Authentication) principal).getPrincipal();
        Long recipientId = userDetails.getId();

        List<ShareResponseDto> shares = shareService.getSharesForRecipient(recipientId);
        return ResponseEntity.ok(shares);
    }

    @Operation(summary = "Mark share as read", description = "Marks a share (message) as read for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share updated as read"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized"),
            @ApiResponse(responseCode = "404", description = "Share not found")
    })
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

    @Operation(summary = "Mark share as unread", description = "Marks a share (message) as unread for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share updated as unread"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized"),
            @ApiResponse(responseCode = "404", description = "Share not found")
    })
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
