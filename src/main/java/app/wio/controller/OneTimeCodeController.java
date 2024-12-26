package app.wio.controller;

import app.wio.entity.OneTimeCode;
import app.wio.service.OneTimeCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/onetime-codes")
public class OneTimeCodeController {

    private final OneTimeCodeService oneTimeCodeService;

    @Autowired
    public OneTimeCodeController(OneTimeCodeService oneTimeCodeService) {
        this.oneTimeCodeService = oneTimeCodeService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OneTimeCode> generateCode(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "24") int expirationInHours
    ) {
        OneTimeCode code = oneTimeCodeService.generateCode(companyId, expirationInHours);
        return ResponseEntity.status(HttpStatus.CREATED).body(code);
    }

    @GetMapping("/{code}")
    public ResponseEntity<OneTimeCode> getCodeInfo(@PathVariable String code) {
        OneTimeCode otc = oneTimeCodeService.getCodeByValue(code);
        return ResponseEntity.ok(otc);
    }

    @GetMapping("/company/{companyId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OneTimeCode>> getActiveCodesForCompany(
            @PathVariable Long companyId
    ) {
        List<OneTimeCode> codes = oneTimeCodeService.getActiveCodesForCompany(companyId);
        return ResponseEntity.ok(codes);
    }
}
