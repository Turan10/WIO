package app.wio.service;

import app.wio.dto.SeatBookingInfoDto;
import app.wio.dto.SeatDto;
import app.wio.entity.*;
import app.wio.exception.FloorNotFoundException;
import app.wio.exception.SeatNotFoundException;
import app.wio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final FloorRepository floorRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository,
                       FloorRepository floorRepository,
                       BookingRepository bookingRepository) {
        this.seatRepository = seatRepository;
        this.floorRepository = floorRepository;
        this.bookingRepository = bookingRepository;
    }

    public boolean existsByFloorId(Long floorId) {
        return seatRepository.existsByFloorId(floorId);
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new SeatNotFoundException("Seat with ID " + id + " not found."));
    }

    public Seat getSeatByIdAndFloorId(Long id, Long floorId) {
        return seatRepository.findByIdAndFloorId(id, floorId)
                .orElseThrow(() -> new SeatNotFoundException("Seat not found."));
    }

    public Seat createSeat(SeatDto seatDto) {
        // Map SeatDto to Seat entity
        Seat seat = new Seat();
        seat.setSeatNumber(seatDto.getSeatNumber());
        seat.setXCoordinate(seatDto.getXCoordinate());
        seat.setYCoordinate(seatDto.getYCoordinate());
        seat.setStatus(seatDto.getStatus());

        // Fetch the floor
        Floor floor = floorRepository.findById(seatDto.getFloorId())
                .orElseThrow(() -> new FloorNotFoundException("Floor not found."));
        seat.setFloor(floor);

        // Check for unique seat number on the floor
        if (seatRepository.existsBySeatNumberAndFloorId(seatDto.getSeatNumber(), seatDto.getFloorId())) {
            throw new IllegalArgumentException("Seat number already exists on this floor.");
        }

        return seatRepository.save(seat);
    }

    public Seat updateSeat(Long id, SeatDto seatDto) {
        Seat existingSeat = getSeatById(id);

        // Validate floor existence
        Floor floor = floorRepository.findById(seatDto.getFloorId())
                .orElseThrow(() -> new FloorNotFoundException("Floor not found."));

        // Check for unique seat number if changed
        if (!existingSeat.getSeatNumber().equals(seatDto.getSeatNumber())) {
            if (seatRepository.existsBySeatNumberAndFloorId(seatDto.getSeatNumber(), seatDto.getFloorId())) {
                throw new IllegalArgumentException("Seat number already exists on this floor.");
            }
        }

        // Update seat details
        existingSeat.setSeatNumber(seatDto.getSeatNumber());
        existingSeat.setXCoordinate(seatDto.getXCoordinate());
        existingSeat.setYCoordinate(seatDto.getYCoordinate());
        existingSeat.setStatus(seatDto.getStatus());
        existingSeat.setFloor(floor);

        return seatRepository.save(existingSeat);
    }

    public void deleteSeat(Long id) {
        Seat seat = getSeatById(id);
        seatRepository.delete(seat);
    }

    public List<Seat> getAvailableSeatsByFloorIdAndDate(Long floorId, LocalDate date) {

        List<Seat> allSeats = seatRepository.findByFloorId(floorId);

        // Get IDs of seats that are booked on the given date
        List<Long> bookedSeatIds = bookingRepository.findBookedSeatIdsByFloorIdAndDate(floorId, date);

        // Filter out booked seats
        return allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()))
                .collect(Collectors.toList());
    }

    public List<SeatBookingInfoDto> getSeatBookings(Long floorId, LocalDate date) {
        List<Seat> allSeats = seatRepository.findByFloorId(floorId);

        // Get bookings for the given date
        List<Booking> bookings = bookingRepository.findByFloorIdAndDate(floorId, date);

        Map<Long, String> seatBookingsMap = bookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getSeat().getId(),
                        booking -> booking.getUser().getName()
                ));

        return allSeats.stream()
                .map(seat -> {
                    SeatBookingInfoDto dto = new SeatBookingInfoDto();
                    dto.setSeatId(seat.getId());
                    dto.setSeatNumber(seat.getSeatNumber());
                    if (seatBookingsMap.containsKey(seat.getId())) {
                        dto.setBooked(true);
                        dto.setUserName(seatBookingsMap.get(seat.getId()));
                    } else {
                        dto.setBooked(false);
                        dto.setUserName(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}