package app.wio.controller;

import app.wio.dto.*;
import app.wio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register a new user with invite token
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @Valid @RequestBody UserRegistrationDto userDto) {
        UserResponseDto response = userService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Verify user email
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("User verified successfully.");
    }

    // Authenticate user (login)
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> authenticateUser(
            @Valid @RequestBody UserLoginDto loginDto) {
        UserResponseDto response = userService.authenticateUser(loginDto.getEmail(), loginDto.getPassword());
        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Update user profile
    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    // Change password
    @PostMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePassword(id, changePasswordDto);
        return ResponseEntity.noContent().build();
    }

    // Delete user account
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Get all users (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Initiate password reset
    @PostMapping("/password-reset/initiate")
    public ResponseEntity<String> initiatePasswordReset(@RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok("Password reset email sent.");
    }

    // Complete password reset
    @PostMapping("/password-reset/complete")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successful.");
    }
}