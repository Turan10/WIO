package app.wio.controller;

import app.wio.dto.SeatBookingInfoDto;
import app.wio.dto.SeatDto;
import app.wio.dto.request.BulkSeatUpdateRequest;
import app.wio.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatDto> createSeat(@Valid @RequestBody SeatDto seatDto) {
        SeatDto seat = seatService.createSeat(seatDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatDto> getSeatById(@PathVariable Long id) {
        SeatDto seat = seatService.getSeatDtoById(id);
        return ResponseEntity.ok(seat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatDto> updateSeat(
            @PathVariable Long id,
            @Valid @RequestBody SeatDto seatDto
    ) {
        SeatDto updatedSeat = seatService.updateSeat(id, seatDto);
        return ResponseEntity.ok(updatedSeat);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@RequestParam Long floorId) {
        List<SeatDto> seats = seatService.getAvailableSeatsByFloorId(floorId);
        return ResponseEntity.ok(seats);
    }


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

    @PostMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkUpdateSeats(@Valid @RequestBody BulkSeatUpdateRequest request) {
        seatService.bulkUpdateSeats(request.getSeats());
        return ResponseEntity.ok("Seats updated");
    }
}
