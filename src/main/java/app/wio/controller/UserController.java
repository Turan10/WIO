package app.wio.controller;

import app.wio.dto.ChangePasswordDto;
import app.wio.dto.UserLoginDto;
import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserUpdateDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Endpoints for user registration, login, profile updates, and management")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user", description = "Registers a user (employee) with one-time code. For Admin, omit the oneTimeCode.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationDto userDto) {
        UserResponseDto response = userService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login user", description = "Authenticates a user by email/password, returns JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> authenticateUser(@Valid @RequestBody UserLoginDto loginDto) {
        UserResponseDto response = userService.authenticateUser(loginDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves user details (admin or the user themself).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user", description = "Updates a user’s profile. Only the user themself can do this.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Change password", description = "Changes the password for the logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Invalid old password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePassword(id, changePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete user", description = "Deletes a user’s account (the user themself only).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users", description = "Retrieves all users (admin-only).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Initiate password reset", description = "Sends a password reset email to the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset initiated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/password-reset/initiate")
    public ResponseEntity<String> initiatePasswordReset(@RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok("Password reset email sent.");
    }

    @Operation(summary = "Complete password reset", description = "Resets a user’s password with a valid token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid token or new password missing")
    })
    @PostMapping("/password-reset/complete")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successful.");
    }
}
