// package app.wio.dto.response;

package app.wio.dto.response;

import app.wio.entity.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String token;
    private Long companyId;
    private String companyName;
    private String title;
    private String department;
    private String phone;
    private String avatar;
}
