package app.wio.dto;

import app.wio.dto.validation.ValidOneTimeCodeForEmployee;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidOneTimeCodeForEmployee
public class UserRegistrationDto {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit."
    )
    private String password;

    @NotNull(message = "Role is required.")
    private UserRoleDto role;


    private String oneTimeCode;
}
