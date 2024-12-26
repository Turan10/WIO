package app.wio.dto.response;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FloorDto {
    private Long id;
    private String name;
    private Integer floorNumber;
    private Long companyId;
    private List<Long> seatIds;
}
