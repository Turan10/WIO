package app.wio.service;

import app.wio.dto.*;
import app.wio.entity.*;
import app.wio.exception.*;
import app.wio.repository.*;
import app.wio.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final InviteService inviteService;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       InviteService inviteService,
                       VerificationTokenRepository tokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.inviteService = inviteService;
        this.tokenRepository = tokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    // Register a new user with invite token
    public UserResponseDto registerUser(UserRegistrationDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        Invite invite = inviteService.getInviteByToken(userDto.getInviteToken());

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.EMPLOYEE);
        user.setCompany(invite.getCompany());
        user.setEnabled(false); // User needs to verify email

        userRepository.save(user);

        // Create verification token
        VerificationToken verificationToken = new VerificationToken(user, 24);
        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user, verificationToken.getToken());

        return mapToUserResponseDto(user, null);
    }

    // Verify user email
    @Transactional
    public void verifyUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token."));

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Clean up token
        tokenRepository.delete(verificationToken);
    }

    public UserResponseDto authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        // Debugging statements
        System.out.println("DB PASS: " + user.getPassword());
        boolean match = passwordEncoder.matches(password, user.getPassword());
        System.out.println("MATCH: " + match);

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Please verify your email before logging in.");
        }

        if (!match) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = jwtTokenProvider.generateToken(user);

        return mapToUserResponseDto(user, token);
    }


    // Get a user by ID
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return mapToUserResponseDto(user, null);
    }

    // Update user profile
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null && !user.getEmail().equals(userUpdateDto.getEmail())) {
            if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new EmailAlreadyInUseException("Email already in use.");
            }
            user.setEmail(userUpdateDto.getEmail());
        }

        userRepository.save(user);

        return mapToUserResponseDto(user, null);
    }

    // Change password
    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidOldPasswordException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    // Delete user account
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found.");
        }
        userRepository.deleteById(userId);
    }

    // Get all users (for admin)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> mapToUserResponseDto(user, null))
                .collect(Collectors.toList());
    }

    // Initiate password reset
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        PasswordResetToken resetToken = new PasswordResetToken(user, 1); // 1 hour expiry
        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user, resetToken.getToken());
    }

    // Reset password
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token."));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Clean up token
        passwordResetTokenRepository.delete(resetToken);
    }

    // Helper method to map User to UserResponseDto
    private UserResponseDto mapToUserResponseDto(User user, String token) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setToken(token);
        return dto;
    }
}