package app.wio.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareRequestDto {
    private Long recipientId;
    private List<Long> bookingIds;
    private String message;
}
