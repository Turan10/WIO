package app.wio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Seat number is required.")
    private String seatNumber;

    @NotNull(message = "X coordinate is required.")
    private Double xCoordinate;

    @NotNull(message = "Y coordinate is required.")
    private Double yCoordinate;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Version
    private Integer version;
}