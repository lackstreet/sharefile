package com.company.sharefile.dto.v1.records.response;

import java.util.UUID;

public record FileUploadResponseDTO(
        UUID fileId,
        String fileName,
        Long fileSize,
        String checksum,
        Boolean success
) {
}