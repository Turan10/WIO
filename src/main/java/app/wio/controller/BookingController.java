package app.wio.controller;

import app.wio.dto.request.BookingRequestDto;
import app.wio.dto.response.BookingResponseDto;
import app.wio.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto bookingDto
    ) {
        BookingResponseDto booking = bookingService.createBooking(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isBookingOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<Page<BookingResponseDto>> getBookingsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponseDto> bookings = bookingService.getBookingsByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }


    @GetMapping("/user/{userId}/all")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getBookingsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BookingResponseDto> bookingPage = bookingService.findBookingsForUser(userId, page, size);
        return ResponseEntity.ok(bookingPage);
    }
}
