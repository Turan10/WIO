package app.wio.controller;

import app.wio.dto.SeatBookingInfoDto;
import app.wio.dto.SeatDto;
import app.wio.dto.request.BulkSeatUpdateRequest;
import app.wio.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@Tag(name = "Seat API", description = "Endpoints for creating, updating, and retrieving seats")
public class SeatController {

    private final SeatService seatService;

    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @Operation(summary = "Create a new seat", description = "Creates a seat on a floor (admin-only).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seat created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatDto> createSeat(@Valid @RequestBody SeatDto seatDto) {
        SeatDto seat = seatService.createSeat(seatDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @Operation(summary = "Get seat by ID", description = "Retrieves a specific seat’s details by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat retrieved"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatDto> getSeatById(@PathVariable Long id) {
        SeatDto seat = seatService.getSeatDtoById(id);
        return ResponseEntity.ok(seat);
    }

    @Operation(summary = "Update a seat", description = "Updates seat information, e.g. seat number, position, angle (admin-only).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat updated"),
            @ApiResponse(responseCode = "404", description = "Seat not found"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatDto> updateSeat(
            @PathVariable Long id,
            @Valid @RequestBody SeatDto seatDto
    ) {
        SeatDto updatedSeat = seatService.updateSeat(id, seatDto);
        return ResponseEntity.ok(updatedSeat);
    }

    @Operation(summary = "Delete a seat", description = "Deletes a seat if it has no future active bookings (admin-only).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Seat deleted"),
            @ApiResponse(responseCode = "409", description = "Seat has future active bookings"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get available seats for a floor", description = "Retrieves a list of available seats on a floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of available seats retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@RequestParam Long floorId) {
        List<SeatDto> seats = seatService.getAvailableSeatsByFloorId(floorId);
        return ResponseEntity.ok(seats);
    }

    @Operation(summary = "Get seats by floor", description = "Retrieves seats (optionally with occupant info) for a specific floor on a given date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seats retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
    @GetMapping("/floor/{floorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSeatsByFloorId(
            @PathVariable Long floorId,
            @RequestParam(required = false) String date
    ) {
        if (date == null) {
            List<SeatDto> seatDtos = seatService.getSeatsByFloorId(floorId);
            return ResponseEntity.ok(seatDtos);
        } else {
            LocalDate localDate = LocalDate.parse(date);
            List<SeatBookingInfoDto> occupantList = seatService.getSeatsWithOccupants(floorId, localDate);
            return ResponseEntity.ok(occupantList);
        }
    }

    @Operation(summary = "Bulk update seats", description = "Creates or updates seats in bulk (admin-only).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seats updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkUpdateSeats(@Valid @RequestBody BulkSeatUpdateRequest request) {
        seatService.bulkUpdateSeats(request.getSeats());
        return ResponseEntity.ok("Seats updated");
    }
}
