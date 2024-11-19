package app.wio.controller;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Company;
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
    private final CompanyService companyService;

    @Autowired
    public FloorController(FloorService floorService, CompanyService companyService) {
        this.floorService = floorService;
        this.companyService = companyService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Floor> createFloor(@Valid @RequestBody FloorCreationDto floorDto) {
        // Retrieve the company
        Company company = companyService.getCompanyById(floorDto.getCompanyId());

        // Check to see if a floor with the same name already exists for this company
        if (floorService.getFloorByNameAndCompanyId(floorDto.getName(), floorDto.getCompanyId()) != null) {
            throw new IllegalArgumentException("A floor with the same name already exists for this company.");
        }

        Floor floor = new Floor();
        floor.setName(floorDto.getName());
        floor.setFloorNumber(floorDto.getFloorNumber());
        floor.setCompany(company);

        Floor savedFloor = floorService.createFloor(floor);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFloor);
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