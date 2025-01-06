package app.wio.controller;

import app.wio.dto.request.BookingRequestDto;
import app.wio.dto.response.BookingResponseDto;
import app.wio.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking API", description = "Endpoints for creating, retrieving, and canceling bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a booking", description = "Creates a new booking given seat, user, and date.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto bookingDto
    ) {
        BookingResponseDto booking = bookingService.createBooking(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @Operation(summary = "Cancel a booking", description = "Cancels a booking by ID (admin or booking owner only).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking canceled successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isBookingOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get bookings by user ID", description = "Retrieves paginated bookings for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of bookings retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
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

    @Operation(summary = "Get all bookings for a user", description = "Retrieves all bookings for a specific user (admin or that user).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of bookings retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
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
