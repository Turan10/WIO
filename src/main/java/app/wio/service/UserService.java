package app.wio.service;

import app.wio.dto.*;
import app.wio.dto.response.UserResponseDto;
import app.wio.entity.*;
import app.wio.exception.*;
import app.wio.mapper.UserMapper;
import app.wio.repository.*;
import app.wio.security.CustomUserDetails;
import app.wio.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CompanyRepository companyRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OneTimeCodeRepository oneTimeCodeRepository;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            CompanyRepository companyRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            OneTimeCodeRepository oneTimeCodeRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.companyRepository = companyRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.oneTimeCodeRepository = oneTimeCodeRepository;
    }

    public User createAdminForCompany(Company company, String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Email already exists.");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.ADMIN);
        user.setCompany(company);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyInUseException("Email already exists.");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);

        if (dto.getRole() == UserRoleDto.ADMIN) {
            user.setRole(UserRole.ADMIN);

        } else if (dto.getRole() == UserRoleDto.EMPLOYEE) {
            user.setRole(UserRole.EMPLOYEE);

            String codeValue = dto.getOneTimeCode();
            if (codeValue == null || codeValue.isBlank()) {
                throw new ResourceNotFoundException("One-time code is required for EMPLOYEE registrations.");
            }
            OneTimeCode code = oneTimeCodeRepository.findByCode(codeValue)
                    .orElseThrow(() -> new InvalidTokenException("Invalid one-time code."));
            if (code.isExpired()) {
                throw new ResourceNotFoundException("One-time code has expired.");
            }
            Company company = companyRepository.findById(code.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found for code."));
            user.setCompany(company);
            code.setUsedCount(code.getUsedCount() + 1);
            oneTimeCodeRepository.save(code);

        } else {
            throw new ResourceConflictException("Invalid user role.");
        }

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    public UserResponseDto authenticateUser(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = jwtTokenProvider.generateToken(user);
        UserResponseDto dto = userMapper.toDto(user);
        dto.setToken(token);
        return dto;
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));


        if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }


        if (userUpdateDto.getEmail() != null && !user.getEmail().equals(userUpdateDto.getEmail())) {
            if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new EmailAlreadyInUseException("Email already in use.");
            }
            user.setEmail(userUpdateDto.getEmail());
        }


        if (userUpdateDto.getTitle() != null) {
            user.setTitle(userUpdateDto.getTitle());
        }


        if (userUpdateDto.getDepartment() != null) {
            user.setDepartment(userUpdateDto.getDepartment());
        }


        if (userUpdateDto.getPhone() != null) {
            user.setPhone(userUpdateDto.getPhone());
        }


        if (userUpdateDto.getAvatar() != null) {
            user.setAvatar(userUpdateDto.getAvatar());
        }

        userRepository.save(user);
        return userMapper.toDto(user);
    }


    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidOldPasswordException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found.");
        }
        userRepository.deleteById(userId);
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        PasswordResetToken newResetToken = new PasswordResetToken(user, 24);
        passwordResetTokenRepository.save(newResetToken);
        // Optionally, send an email with the reset link
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token."));
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void removeEmployeeFromCompany(Long employeeId) {
        CustomUserDetails currentUser = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long adminId = currentUser.getId();

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found."));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admin can remove employees");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new ResourceConflictException("Only EMPLOYEE users can be removed.");
        }

        if (employee.getCompany() == null || !employee.getCompany().getId().equals(admin.getCompany().getId())) {
            throw new AccessDeniedException("You cannot remove an employee from another company.");
        }
        employee.setCompany(null);
        userRepository.save(employee);
    }
}
