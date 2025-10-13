package com.company.sharefile.dto.v1.records.response;

import java.util.UUID;

public record UploadFileResponseDTO(
     UUID fileId,
     String fileName,
     Long fileSize,
     String MimeType,
     String checkSumSha256,
     String createdAt
 ){}

