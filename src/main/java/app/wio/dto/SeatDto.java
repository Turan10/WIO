package app.wio.dto;

import app.wio.entity.Floor;
import app.wio.entity.Seat;
import app.wio.entity.SeatStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {

    private Long id;  // For updates

    @NotBlank(message = "Seat number is required.")
    private String seatNumber;

    @NotNull(message = "X coordinate is required.")
    private Double xCoordinate;

    @NotNull(message = "Y coordinate is required.")
    private Double yCoordinate;

    private SeatStatus status;

    @NotNull(message = "Floor ID is required.")
    private Long floorId;

    public Seat toEntity() {
        Seat seat = new Seat();
        seat.setId(this.id);
        seat.setSeatNumber(this.seatNumber);
        seat.setXCoordinate(this.xCoordinate);
        seat.setYCoordinate(this.yCoordinate);
        seat.setStatus(this.status);
        Floor floor = new Floor();
        floor.setId(this.floorId);
        seat.setFloor(floor);
        return seat;
    }
}