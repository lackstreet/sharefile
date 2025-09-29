package com.company.sharefile.dto.v1.request;

import com.company.sharefile.config.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN , message = "Password not strong enough")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 255)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255)
    private String lastName;
}
