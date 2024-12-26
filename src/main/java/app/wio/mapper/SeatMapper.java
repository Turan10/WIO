package app.wio.mapper;

import app.wio.dto.SeatDto;
import app.wio.entity.Floor;
import app.wio.entity.Seat;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "floor.id", target = "floorId")
    SeatDto toDto(Seat seat);

    @Mapping(source = "floorId", target = "floor", qualifiedByName = "floorFromId")
    Seat toEntity(SeatDto dto);

    @Named("floorFromId")
    default Floor floorFromId(Long floorId) {
        if (floorId == null) return null;
        Floor floor = new Floor();
        floor.setId(floorId);
        return floor;
    }
}
