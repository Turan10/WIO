package app.wio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "share_bookings")
public class ShareBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "share_id", nullable = false)
    private Share share;


    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Eventuel extra info, fx localdatetime
    // private LocalDateTime createdAt;

    public ShareBooking(Share share, Booking booking) {
        this.share = share;
        this.booking = booking;
    }
}