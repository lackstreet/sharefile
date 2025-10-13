package com.company.sharefile.dto.v1.records.request;

import com.company.sharefile.config.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UserCreateRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 64)
        @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = "Password not strong enough")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 255)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255)
        String lastName
) {}