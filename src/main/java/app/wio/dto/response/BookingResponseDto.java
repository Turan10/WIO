package app.wio.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long id;
    private LocalDate date;
    private String status;
    private Long userId;
    private Long seatId;
    private String seatNumber;
    private Integer floorNumber;
    private String floorName;
}
