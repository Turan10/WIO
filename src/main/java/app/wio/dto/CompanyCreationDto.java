package app.wio.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreationDto {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Admin name is required.")
    private String adminName;

    @NotBlank(message = "Admin email is required.")
    @Email(message = "Invalid email format.")
    private String adminEmail;

    @NotBlank(message = "Admin password is required.")
    @Size(min = 6, message = "Admin password must be at least 6 characters long.")
    private String adminPassword;
}
