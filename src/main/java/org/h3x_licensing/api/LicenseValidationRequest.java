package org.h3x_licensing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LicenseValidationRequest(
        @NotBlank @Size(max = 80) String licenseKey,
        @NotBlank @Size(max = 120) String productSlug,
        @Size(max = 120) String discordId,
        @Size(max = 120) String fivemServerId
) {
}

