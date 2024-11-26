package app.wio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatBookingInfoDto {
    private Long seatId;
    private String seatNumber;
    private String userName;
    private boolean isBooked;
}