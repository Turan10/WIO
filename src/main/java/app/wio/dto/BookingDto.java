package app.wio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    @NotNull(message = "Seat ID is required.")
    private Long seatId;

    @NotNull(message = "User ID is required.")
    private Long userId;

    @NotNull(message = "Booking date is required.")
    @FutureOrPresent(message = "Booking date cannot be in the past.")
    private LocalDate date;
}