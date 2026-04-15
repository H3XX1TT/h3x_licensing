package org.h3x_licensing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductForm(
        @NotBlank @Size(min = 3, max = 120) String name,
        @NotBlank @Size(min = 3, max = 120) String slug,
        @NotBlank @Size(min = 10, max = 1000) String description
) {
}

