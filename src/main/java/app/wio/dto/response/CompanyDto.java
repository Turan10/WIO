package app.wio.dto.response;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String name;
    private String address;
    private List<Long> floorIds;
    private List<Long> userIds;
    private String adminToken;
}
