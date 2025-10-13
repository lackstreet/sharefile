package com.company.sharefile.dto.v1.records.response;

import java.util.List;
import java.util.UUID;

public record InitTransferResponseDTO(
    UUID transferId,
    List<FileUploadUrlDTO> files
){
    public record FileUploadUrlDTO(
            UUID fileId,
            String name,
            String uploadUrl
    ) {}
}

