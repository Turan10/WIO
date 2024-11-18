package app.wio.service;

import app.wio.dto.*;
import app.wio.entity.*;
import app.wio.exception.*;
import app.wio.repository.*;
import app.wio.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Register a new user
    public UserResponseDto registerUser(UserRegistrationDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.EMPLOYEE);
        user.setCompany(company);

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);

        return mapToUserResponseDto(user, token);
    }

    // Authenticate a user (login)
    public UserResponseDto authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
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