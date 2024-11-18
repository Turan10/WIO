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

    @Min(value = 1, message = "Floor number must be at least 1.")
    private Integer floorNumber;

    @NotNull(message = "Company ID is required.")
    private Long companyId;
}