package com.company.sharefile.dto.v1.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserCreateResponseDTO {
    private UUID id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
}
