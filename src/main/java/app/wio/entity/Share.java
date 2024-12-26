package app.wio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


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
    private String bookingIdsCsv;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDate maxBookingDate;
}
