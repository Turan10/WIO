package app.wio.mapper;

import app.wio.dto.response.FloorDto;
import app.wio.entity.Floor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = java.util.stream.Collectors.class)
public interface FloorMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "floorNumber", source = "floorNumber")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(
            target = "seatIds",
            expression = "java(floor.getSeats() == null ? null : "
                    + "floor.getSeats().stream().map(seat -> seat.getId())"
                    + ".collect(Collectors.toList()))"
    )
    FloorDto toDto(Floor floor);
}
