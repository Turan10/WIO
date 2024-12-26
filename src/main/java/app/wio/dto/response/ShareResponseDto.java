package app.wio.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponseDto {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private List<Long> bookingIds;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDate maxBookingDate;
}
