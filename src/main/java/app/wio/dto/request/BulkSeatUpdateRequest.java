package app.wio.dto.request;

import app.wio.dto.SeatDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkSeatUpdateRequest {
    private List<SeatDto> seats;

}
