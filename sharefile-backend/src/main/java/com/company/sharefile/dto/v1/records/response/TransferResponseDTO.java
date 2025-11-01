package com.company.sharefile.dto.v1.records.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransferResponseDTO (
        UUID id,
        String title,
        String message,
        String shareLink,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long totalSizeBytes,
        List<String> files,
        List<String> recipientEmails
){};
