package app.wio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatBookingInfoDto {

    private Long id;
    private String seatNumber;
    private Double xCoordinate;
    private Double yCoordinate;
    private Integer angle;
    private boolean booked;
    private String occupantName;
}
