package app.wio.controller;

import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.entity.Floor;
import app.wio.entity.User;
import app.wio.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Create a new company
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody CompanyCreationDto companyDto) {
        if (companyDto.getFloorNames().size() != companyDto.getFloorCount()) {
            throw new IllegalArgumentException("The number of floor names must match the floor count.");
        }

        Company company = new Company();
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());

        List<Floor> floorList = new ArrayList<>();
        for (int i = 0; i < companyDto.getFloorCount(); i++) {
            Floor floor = new Floor();
            floor.setFloorNumber(i + 1);
            floor.setName(companyDto.getFloorNames().get(i));
            floor.setCompany(company);
            floorList.add(floor);
        }
        company.setFloors(floorList);

        Company savedCompany = companyService.createCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);
    }

    // Get company by ID
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    // Get users by company ID
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByCompanyId(@PathVariable Long id) {
        List<User> users = companyService.getUsersByCompanyId(id);
        return ResponseEntity.ok(users);
    }
}