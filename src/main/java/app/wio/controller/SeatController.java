package app.wio.controller;

import app.wio.dto.SeatDto;
import app.wio.entity.Seat;
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
    public ResponseEntity<Seat> createSeat(@Valid @RequestBody SeatDto seatDto) {
        Seat seat = seatService.createSeat(seatDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long id) {
        Seat seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Seat> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatDto seatDto) {
        Seat updatedSeat = seatService.updateSeat(id, seatDto);
        return ResponseEntity.ok(updatedSeat);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint to get available seats by floor and date
    @GetMapping("/available")
    public ResponseEntity<List<Seat>> getAvailableSeats(
            @RequestParam Long floorId,
            @RequestParam LocalDate date) {
        List<Seat> seats = seatService.getAvailableSeatsByFloorIdAndDate(floorId, date);
        return ResponseEntity.ok(seats);
    }
}