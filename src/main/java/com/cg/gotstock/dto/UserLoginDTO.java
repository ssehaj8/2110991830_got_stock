package com.cg.gotstock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$",
            message = "Password must contain at least one letter and one number")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    private String email;
}