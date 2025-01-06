package app.wio.controller;

import app.wio.entity.OneTimeCode;
import app.wio.service.OneTimeCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onetime-codes")
@Tag(name = "OneTimeCode API", description = "Endpoints for generating and retrieving one-time codes for employees")
public class OneTimeCodeController {

    private final OneTimeCodeService oneTimeCodeService;

    @Autowired
    public OneTimeCodeController(OneTimeCodeService oneTimeCodeService) {
        this.oneTimeCodeService = oneTimeCodeService;
    }

    @Operation(summary = "Generate a one-time code", description = "Generates a new one-time code for employee registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "One-time code created successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OneTimeCode> generateCode(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "24") int expirationInHours
    ) {
        OneTimeCode code = oneTimeCodeService.generateCode(companyId, expirationInHours);
        return ResponseEntity.status(HttpStatus.CREATED).body(code);
    }

    @Operation(summary = "Get info for a one-time code", description = "Retrieves details of a one-time code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code retrieved"),
            @ApiResponse(responseCode = "404", description = "Code not found or expired")
    })
    @GetMapping("/{code}")
    public ResponseEntity<OneTimeCode> getCodeInfo(@PathVariable String code) {
        OneTimeCode otc = oneTimeCodeService.getCodeByValue(code);
        return ResponseEntity.ok(otc);
    }

    @Operation(summary = "Get active codes for a company", description = "Lists all non-expired codes for a given company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active codes retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden or not authorized")
    })
    @GetMapping("/company/{companyId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OneTimeCode>> getActiveCodesForCompany(
            @PathVariable Long companyId
    ) {
        List<OneTimeCode> codes = oneTimeCodeService.getActiveCodesForCompany(companyId);
        return ResponseEntity.ok(codes);
    }
}
