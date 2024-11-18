package app.wio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreationDto {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotNull(message = "Floor count is required.")
    @Min(value = 1, message = "There must be at least one floor.")
    private Integer floorCount;

    @NotNull(message = "Floor names are required.")
    @Size(min = 1, message = "At least one floor name is required.")
    private List<@NotBlank String> floorNames;
}