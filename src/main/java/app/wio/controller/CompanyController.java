package app.wio.controller;

import app.wio.dto.CompanyCreationDto;
import app.wio.dto.response.CompanyDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.service.CompanyService;
import app.wio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final UserService userService;

    @Autowired
    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<CompanyDto> createCompany(
            @Valid @RequestBody CompanyCreationDto companyDto
    ) {
        CompanyDto company = companyService.createCompany(companyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        CompanyDto company = companyService.getCompanyDtoById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserResponseDto>> getUsersByCompanyId(@PathVariable Long id) {
        List<UserResponseDto> users = companyService.getUsersByCompanyId(id);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{companyId}/users/{employeeId}")
    public ResponseEntity<Void> removeEmployeeFromCompany(
            @PathVariable Long companyId,
            @PathVariable Long employeeId
    ) {
        userService.removeEmployeeFromCompany(employeeId);
        return ResponseEntity.noContent().build();
    }
}
