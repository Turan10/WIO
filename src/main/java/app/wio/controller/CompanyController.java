package app.wio.controller;

import app.wio.dto.CompanyCreationDto;
import app.wio.dto.response.CompanyDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.service.CompanyService;
import app.wio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company API", description = "Endpoints for creating and managing companies")
public class CompanyController {

    private final CompanyService companyService;
    private final UserService userService;

    @Autowired
    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new company", description = "Creates a company and an associated admin user in one flow.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "409", description = "Company name already in use"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/create")
    public ResponseEntity<CompanyDto> createCompany(
            @Valid @RequestBody CompanyCreationDto companyDto
    ) {
        CompanyDto company = companyService.createCompany(companyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @Operation(summary = "Get company by ID", description = "Retrieves a specific company’s details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company retrieved"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        CompanyDto company = companyService.getCompanyDtoById(id);
        return ResponseEntity.ok(company);
    }

    @Operation(summary = "Get users for a company", description = "Retrieves all users belonging to a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users retrieved"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserResponseDto>> getUsersByCompanyId(@PathVariable Long id) {
        List<UserResponseDto> users = companyService.getUsersByCompanyId(id);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Remove employee from company", description = "Removes an employee from a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee removed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{companyId}/users/{employeeId}")
    public ResponseEntity<Void> removeEmployeeFromCompany(
            @PathVariable Long companyId,
            @PathVariable Long employeeId
    ) {
        userService.removeEmployeeFromCompany(employeeId);
        return ResponseEntity.noContent().build();
    }
}
