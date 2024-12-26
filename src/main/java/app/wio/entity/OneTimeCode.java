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
@Table(name = "one_time_codes")
public class OneTimeCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which company this code is for
    private Long companyId;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Could track usage count, if you want to limit how many employees can use it
    private int usedCount;

    public OneTimeCode(Long companyId, int expirationInHours) {
        this.companyId = companyId;
        this.code = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase();
        this.expiryDate = LocalDateTime.now().plusHours(expirationInHours);
        this.usedCount = 0;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
