package com.company.sharefile.dto.v1.records.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record TransferRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be less than 255 characters")
        String title,

        @Size(max = 2048, message = "Message must be less than 2048 characters")
        String message,

        @Min(value = 1, message = "Expires in days must be at least 1")
        @Max(value = 30, message = "Expires in days cannot exceed 30")
        Long expiresInDays,

        Boolean notifyRecipient,

        @NotNull(message = "Recipient emails list cannot be null")
        @NotEmpty(message = "At least one recipient email is required")
        @Size(max = 10, message = "Maximum 10 recipients per transfer")
        List<@NotBlank @Email(message = "Invalid email format") String> recipientEmails,

        @NotNull(message = "Files list cannot be null")
        @NotEmpty(message = "At least one file is required")
        @Size(max = 50, message = "Maximum 50 files per transfer")
        List<@NotBlank String> files
) {}
