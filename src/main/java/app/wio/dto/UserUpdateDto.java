package app.wio.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    private String name;

    @Email(message = "Invalid email format.")
    private String email;
}