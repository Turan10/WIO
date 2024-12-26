package app.wio.service;

import app.wio.dto.SeatDto;
import app.wio.dto.SeatBookingInfoDto;
import app.wio.entity.*;
import app.wio.exception.ResourceConflictException;
import app.wio.exception.ResourceNotFoundException;
import app.wio.mapper.SeatMapper;
import app.wio.repository.BookingRepository;
import app.wio.repository.FloorRepository;
import app.wio.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final FloorRepository floorRepository;
    private final SeatMapper seatMapper;
    private final BookingRepository bookingRepository;

    @Autowired
    public SeatService(
            SeatRepository seatRepository,
            FloorRepository floorRepository,
            SeatMapper seatMapper,
            BookingRepository bookingRepository
    ) {
        this.seatRepository = seatRepository;
        this.floorRepository = floorRepository;
        this.seatMapper = seatMapper;
        this.bookingRepository = bookingRepository;
    }


    public SeatDto createSeat(SeatDto seatDto) {
        Floor floor = floorRepository.findById(seatDto.getFloorId())
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found"));

        Seat seat = seatMapper.toEntity(seatDto);
        seat.setFloor(floor);
        Seat saved = seatRepository.save(seat);
        return seatMapper.toDto(saved);
    }


    public SeatDto getSeatDtoById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        return seatMapper.toDto(seat);
    }


    public SeatDto updateSeat(Long id, SeatDto seatDto) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        seat.setSeatNumber(seatDto.getSeatNumber());
        seat.setXCoordinate(seatDto.getXCoordinate());
        seat.setYCoordinate(seatDto.getYCoordinate());
        seat.setAngle(seatDto.getAngle());
        seat.setStatus(seatDto.getStatus());

        Seat saved = seatRepository.save(seat);
        return seatMapper.toDto(saved);
    }


    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        LocalDate today = LocalDate.now();
        List<Booking> futureBookings = bookingRepository.findBySeatIdAndDateAfterAndStatus(
                id, today, BookingStatus.ACTIVE
        );
        if (!futureBookings.isEmpty()) {
            throw new ResourceConflictException(
                    "Cannot delete seat; it has future active bookings."
            );
        }

        seatRepository.delete(seat);
    }

    public List<SeatDto> getSeatsByFloorId(Long floorId) {
        List<Seat> seats = seatRepository.findByFloorId(floorId);
        return seats.stream()
                .map(seatMapper::toDto)
                .collect(Collectors.toList());
    }


    public List<SeatDto> getAvailableSeatsByFloorId(Long floorId) {
        return getSeatsByFloorId(floorId).stream()
                .filter(s -> s.getStatus() != null && s.getStatus().name().equals("AVAILABLE"))
                .collect(Collectors.toList());
    }


    public List<SeatBookingInfoDto> getSeatsWithOccupants(Long floorId, LocalDate date) {
        // fetch seats for the floor
        List<Seat> seats = seatRepository.findByFloorId(floorId);

        return seats.stream().map(seat -> {
            SeatBookingInfoDto dto = new SeatBookingInfoDto();
            dto.setId(seat.getId());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setXCoordinate(seat.getXCoordinate());
            dto.setYCoordinate(seat.getYCoordinate());
            dto.setAngle(seat.getAngle());

            List<Booking> seatBookings = bookingRepository.findBySeatIdAndDate(seat.getId(), date);
            Optional<Booking> activeBooking = seatBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.ACTIVE)
                    .findFirst();

            if (activeBooking.isPresent()) {
                Booking b = activeBooking.get();
                dto.setBooked(true);
                dto.setOccupantName(b.getUser() != null ? b.getUser().getName() : "Unknown");
            } else {
                dto.setBooked(false);
                dto.setOccupantName(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void bulkUpdateSeats(List<SeatDto> seatDtos) {
        for (SeatDto dto : seatDtos) {
            if (dto.getId() == null) {
                createSeat(dto);
            } else {
                updateSeat(dto.getId(), dto);
            }
        }
    }
}
