package org.h3x_licensing.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.h3x_licensing.license.LicenseSource;
import org.h3x_licensing.license.LicenseStatus;

public record CreateLicenseForm(
        @NotNull Long productId,
        @NotBlank @Size(min = 3, max = 120) String buyerName,
        @NotBlank @Email String buyerEmail,
        @NotBlank @Size(min = 4, max = 120) String purchaseReference,
        @NotNull LicenseSource source,
        @NotNull LicenseStatus status,
        @Size(max = 120) String discordId,
        @Size(max = 120) String fivemServerId
) {
}

