package app.wio.controller;

import app.wio.dto.BookingDto;
import app.wio.entity.Booking;
import app.wio.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a new booking
    @PostMapping("/create")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingDto bookingDto) {
        Booking booking = bookingService.createBooking(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // Cancel a booking
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isBookingOwner(#id, principal.id)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // Get bookings by user ID
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    public ResponseEntity<Page<Booking>> getBookingsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookings = bookingService.getBookingsByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }
}