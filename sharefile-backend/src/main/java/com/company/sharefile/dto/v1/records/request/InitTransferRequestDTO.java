package com.company.sharefile.dto.v1.records.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InitTransferRequestDTO(
    @Size(max = 255)
    String message,

    List<FileMetaDataDTO> files
){
    public record FileMetaDataDTO(
            @NotBlank
            String fileName,
            @NotBlank
            Long fileSize
    ){}
}

