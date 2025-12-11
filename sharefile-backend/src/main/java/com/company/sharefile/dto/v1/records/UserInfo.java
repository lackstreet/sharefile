package com.company.sharefile.dto.v1.records;

import lombok.Builder;

import java.util.Set;
@Builder
public record UserInfo(
        String username,
        String email,
        String name,
        Set<String> roles
) {
    public UserInfo {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (roles == null) {
            roles = Set.of();
        }
    }
}
