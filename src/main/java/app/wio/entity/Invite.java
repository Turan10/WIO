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
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public Invite(Company company, int expirationInHours) {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(expirationInHours);
        this.company = company;
    }
}