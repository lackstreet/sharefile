package com.company.sharefile.dto.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @NotBlank(message = "Email is required")
    private String username;
    @NotNull(message = "Password is required")
    private String password;
}
