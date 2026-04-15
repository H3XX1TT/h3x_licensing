package org.h3x_licensing.api;

public record LicenseValidationResponse(
        boolean valid,
        String status,
        String message,
        String licenseKey,
        String productSlug,
        String ownerEmail
) {
}

