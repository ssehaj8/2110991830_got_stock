package com.cg.gotstock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    private Long id;

    @NotBlank(message = "FirstName is mandatory")
    @Size(min = 3, max = 20, message = "firstname must be between 3 and 20 characters")
    private String firstname;

    @NotBlank(message = "Lastname is mandatory")
    @Size(min = 3, max = 20, message = "lastname must be between 3 and 20 characters")
    private String lastname;

    @NotBlank(message = "Username is mandatory")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number cannot be empty")
    @Size(max = 10, message = "Phone number should be of 10 digits")
    private String phonenumber;
}
