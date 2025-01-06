package app.wio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shares")
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long recipientId;

    @Column(length = 1000)
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDate maxBookingDate;


    @OneToMany(mappedBy = "share", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShareBooking> shareBookings = new ArrayList<>();
}