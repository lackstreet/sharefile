package com.company.sharefile.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
public enum Role {
    ADMIN("admin"),
    MANAGER("manager"),
    USER("user"),
    GUEST("guest");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

}
