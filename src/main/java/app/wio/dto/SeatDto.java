package app.wio.dto;

import app.wio.entity.SeatStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {

    private Long id;

    @NotBlank(message = "Seat number is required.")
    private String seatNumber;

    @JsonProperty("xCoordinate")
    @NotNull(message = "X coordinate is required.")
    private Double xCoordinate;

    @JsonProperty("yCoordinate")
    @NotNull(message = "Y coordinate is required.")
    private Double yCoordinate;

    private SeatStatus status;

    @NotNull(message = "Floor ID is required.")
    @Min(value = 1, message = "Floor ID must be greater than 0.")
    private Long floorId;

    private Integer angle;
}
