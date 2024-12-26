package app.wio.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seats")
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

    private Integer angle; // rotation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    @JsonBackReference
    private Floor floor;
    @Version
    private Integer version;
}
