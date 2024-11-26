package app.wio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public PasswordResetToken(User user, int expirationInHours) {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(expirationInHours);
        this.user = user;
    }
}