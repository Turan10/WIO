package app.wio.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FloorCreationDto {

    @NotBlank(message = "Floor name is required.")
    private String name;

    @NotNull(message = "Floor number is required.")
    @Min(value = 1, message = "Floor number must be at least 1.")
    private Integer floorNumber;

    @NotNull(message = "Company ID is required.")
    @Min(value = 1, message = "Company ID must be greater than 0.")
    private Long companyId;
}
