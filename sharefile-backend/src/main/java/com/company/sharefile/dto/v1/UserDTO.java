package com.company.sharefile.dto.v1;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String email;
    private int id;
    private String password;
    private String username;
    private String firstName;
    private String lastName;
}
