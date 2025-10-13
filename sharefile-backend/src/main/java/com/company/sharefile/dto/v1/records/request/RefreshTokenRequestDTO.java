package com.company.sharefile.dto.v1.records.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token is required")
        @Size(min = 10, message = "Refresh token must be at least 10 characters long")
        String refreshToken
) {}