package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.dto.response.FloorDto;
import app.wio.mapper.FloorMapper;
import app.wio.entity.Floor;
import app.wio.security.CustomUserDetails;
import app.wio.service.FloorService;
import app.wio.service.SeatService;
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

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FloorDto> createFloor(
            @Valid @RequestBody FloorCreationDto floorDto
    ) {
        Floor floor = floorService.createFloor(floorDto);
        FloorDto floorResponse = floorMapper.toDto(floor);
        return ResponseEntity.status(HttpStatus.CREATED).body(floorResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFloorById(@PathVariable Long id) {
        floorService.deleteFloorById(id);
        return ResponseEntity.noContent().build();
    }

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
