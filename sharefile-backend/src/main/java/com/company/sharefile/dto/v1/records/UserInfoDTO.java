package com.company.sharefile.dto.v1.records;

import java.util.UUID;

public record UserInfoDTO(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    String company,
    String department,
    Long storageQuotaBytes,
    Long usedStorageBytes,
    Boolean isActive
){}
