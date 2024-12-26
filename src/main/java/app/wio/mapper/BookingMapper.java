package app.wio.mapper;

import app.wio.dto.request.BookingRequestDto;
import app.wio.dto.response.BookingResponseDto;
import app.wio.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "seat.id", target = "seatId")
    @Mapping(source = "seat.seatNumber", target = "seatNumber")
    @Mapping(source = "seat.floor.floorNumber", target = "floorNumber")
    @Mapping(source = "seat.floor.name", target = "floorName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "id", target = "id")
    BookingResponseDto toDto(Booking booking);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "seatId", target = "seat.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    Booking toEntity(BookingRequestDto dto);
}
