package app.wio.service;

import app.wio.dto.request.BookingRequestDto;
import app.wio.dto.response.BookingResponseDto;
import app.wio.entity.*;
import app.wio.exception.*;
import app.wio.mapper.BookingMapper;
import app.wio.repository.BookingRepository;
import app.wio.repository.SeatRepository;
import app.wio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            SeatRepository seatRepository,
            UserRepository userRepository,
            BookingMapper bookingMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    public Page<BookingResponseDto> getBookingsByUserId(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return bookings.map(bookingMapper::toDto);
    }

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto dto) {
        Seat seat = seatRepository.findById(dto.getSeatId())
                .orElseThrow(() -> new SeatNotFoundException("Seat with ID " + dto.getSeatId() + " not found."));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + dto.getUserId() + " not found."));


        boolean hasExistingBooking = bookingRepository.existsByUserIdAndDate(user.getId(), dto.getDate());
        if (hasExistingBooking) {
            throw new DuplicateBookingException("You have already booked a seat for this date.");
        }

        boolean isSeatBooked = bookingRepository
                .findBySeatIdAndDate(seat.getId(), dto.getDate())
                .stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.ACTIVE);

        if (isSeatBooked) {
            throw new SeatAlreadyBookedException("Seat is already booked on " + dto.getDate() + ".");
        }

        Booking booking = bookingMapper.toEntity(dto);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setSeat(seat);
        booking.setUser(user);

        try {
            Booking saved = bookingRepository.save(booking);
            return bookingMapper.toDto(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new SeatAlreadyBookedException("Seat was concurrently booked by another user.");
        }
    }

    public BookingResponseDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found."));
        return bookingMapper.toDto(booking);
    }

    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found."));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public boolean isBookingOwner(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException("Booking with ID " + bookingId + " not found."));
        return booking.getUser().getId().equals(userId);
    }

    public Page<BookingResponseDto> findBookingsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bookingRepository.findBookingsByUserIdWithFloor(userId, pageable);
    }
}
