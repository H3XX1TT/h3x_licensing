package org.h3x_licensing.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimLicenseForm(
        @NotBlank @Email String buyerEmail,
        @NotBlank @Size(min = 4, max = 120) String purchaseReference
) {
}

