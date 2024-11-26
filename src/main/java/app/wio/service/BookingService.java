package app.wio.service;

import app.wio.dto.BookingDto;
import app.wio.entity.*;
import app.wio.exception.*;
import app.wio.repository.BookingRepository;
import app.wio.repository.SeatRepository;
import app.wio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          SeatRepository seatRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    // Get all bookings by user ID
    public Page<Booking> getBookingsByUserId(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable);
    }

    // Create a new booking
    @Transactional
    public Booking createBooking(BookingDto bookingDto) {

        // Check if seat exists
        Seat seat = seatRepository.findById(bookingDto.getSeatId())
                .orElseThrow(() -> new SeatNotFoundException("Seat not found."));

        // Check if user exists
        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        // Check if seat is available on the given date
        if (bookingRepository.findBySeatIdAndDate(seat.getId(), bookingDto.getDate())
                .stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.ACTIVE)) {
            throw new IllegalArgumentException("Seat is already booked on this date.");
        }

        Booking booking = new Booking();
        booking.setSeat(seat);
        booking.setUser(user);
        booking.setDate(bookingDto.getDate());
        booking.setStatus(BookingStatus.ACTIVE);

        try {
            return bookingRepository.save(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalArgumentException("Seat is already booked by someone else.");
        }
    }

    // Get booking by ID
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found."));
    }

    // Cancel a booking
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // Check if the booking belongs to the user
    public boolean isBookingOwner(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found."));
        return booking.getUser().getId().equals(userId);
    }
}