package com.autotrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DriverRequest(
        @NotBlank @Size(max = 140) String fullName,
        @NotBlank @Size(max = 60) String licenseNumber,
        @Size(max = 40) String phone,
        Boolean active
) {}
