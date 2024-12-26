package app.wio.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "Seat ID is required.")
    @Min(value = 1, message = "Seat ID must be greater than 0.")
    private Long seatId;

    @NotNull(message = "User ID is required.")
    @Min(value = 1, message = "User ID must be greater than 0.")
    private Long userId;

    @NotNull(message = "Booking date is required.")
    @FutureOrPresent(message = "Booking date cannot be in the past.")
    private LocalDate date;
}
