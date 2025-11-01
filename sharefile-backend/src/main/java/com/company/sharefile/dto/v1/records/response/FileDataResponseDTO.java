package com.company.sharefile.dto.v1.records.response;

public record FileDataResponseDTO(
    String fileName,
    String mimeType,
    Long fileSize,
    byte[] fileData
) {
}
