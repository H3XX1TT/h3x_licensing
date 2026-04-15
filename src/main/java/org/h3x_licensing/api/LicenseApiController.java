package org.h3x_licensing.api;

import jakarta.validation.Valid;
import org.h3x_licensing.license.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LicenseApiController {

    private final LicenseService licenseService;

    public LicenseApiController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(java.util.Map.of("status", "UP"));
    }

    @PostMapping("/licenses/validate")
    public ResponseEntity<LicenseValidationResponse> validate(@Valid @RequestBody LicenseValidationRequest request) {
        return ResponseEntity.ok(licenseService.validateLicense(request));
    }
}

