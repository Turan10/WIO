package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Floor;
import app.wio.service.CompanyService;
import app.wio.service.FloorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors")
public class FloorController {

    private final FloorService floorService;

    @Autowired
    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Floor> createFloor(@Valid @RequestBody FloorCreationDto floorDto) {
        Floor floor = floorService.createFloor(floorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(floor);
    }

    // Delete floor by id (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFloorById(@PathVariable Long id) {
        floorService.deleteFloorById(id);
        return ResponseEntity.noContent().build();
    }

    // Get all floors by company id
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Floor>> getFloorsByCompanyId(@PathVariable Long companyId) {
        List<Floor> floors = floorService.getFloorsByCompanyId(companyId);
        return ResponseEntity.ok(floors);
    }
}