package com.company.sharefile.dto.v1.records.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public record AuthenticationRequestDTO(
        @Email(message = "Invalid email format")
        @Size(max = 255)
        @NotBlank(message = "Email is required")
        String username,

        @NotNull(message = "Password is required")
        String password
) {}