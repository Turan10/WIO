package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.dto.response.FloorDto;
import app.wio.mapper.FloorMapper;
import app.wio.entity.Floor;
import app.wio.security.CustomUserDetails;
import app.wio.service.FloorService;
import app.wio.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/floors")
@Tag(name = "Floor API", description = "Endpoints for creating, deleting, and locking floors")
public class FloorController {

    private final FloorService floorService;
    private final SeatService seatService;
    private final FloorMapper floorMapper;

    @Autowired
    public FloorController(
            FloorService floorService,
            SeatService seatService,
            FloorMapper floorMapper
    ) {
        this.floorService = floorService;
        this.seatService = seatService;
        this.floorMapper = floorMapper;
    }

    @Operation(summary = "Create a new floor", description = "Creates a floor for a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Floor created successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FloorDto> createFloor(
            @Valid @RequestBody FloorCreationDto floorDto
    ) {
        Floor floor = floorService.createFloor(floorDto);
        FloorDto floorResponse = floorMapper.toDto(floor);
        return ResponseEntity.status(HttpStatus.CREATED).body(floorResponse);
    }

    @Operation(summary = "Delete a floor by ID", description = "Deletes a floor if it has no future active bookings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Floor deleted successfully"),
            @ApiResponse(responseCode = "409", description = "Floor has future active bookings"),
            @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFloorById(@PathVariable Long id) {
        floorService.deleteFloorById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get floors by company ID", description = "Retrieves a list of floors for a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of floors retrieved"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/company/{companyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FloorDto>> getFloorsByCompanyId(
            @PathVariable Long companyId
    ) {
        List<Floor> floors = floorService.getFloorsByCompanyId(companyId);
        List<FloorDto> floorDtos = floors.stream()
                .map(floorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(floorDtos);
    }

    @Operation(summary = "Lock a floor", description = "Locks a floor for editing by a specific user (admin only).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Floor locked successfully"),
            @ApiResponse(responseCode = "409", description = "Floor already locked by another user"),
            @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    @PostMapping("/{floorId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> lockFloor(
            @PathVariable Long floorId,
            Authentication authentication
    ) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        floorService.lockFloor(floorId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unlock a floor", description = "Unlocks a floor that the current admin user had locked.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Floor unlocked successfully"),
            @ApiResponse(responseCode = "409", description = "You do not hold the lock for this floor"),
            @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    @DeleteMapping("/{floorId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlockFloor(
            @PathVariable Long floorId,
            Authentication authentication
    ) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        floorService.unlockFloor(floorId, userId);
        return ResponseEntity.noContent().build();
    }
}
