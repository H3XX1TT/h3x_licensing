package org.h3x_licensing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordForm(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(min = 8, max = 100) String confirmPassword
) {
}

