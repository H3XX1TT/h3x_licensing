package org.h3x_licensing.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 120) String displayName,
        @Size(max = 120) String discordId,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}

