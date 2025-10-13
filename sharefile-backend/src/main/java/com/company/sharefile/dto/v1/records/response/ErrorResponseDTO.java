package com.company.sharefile.dto.v1.records.response;

import lombok.Getter;
import lombok.Setter;

public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        String internalDocumentationErrorCode
) {}