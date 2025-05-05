package com.cg.gotstock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {




    private Long id;

    @NotBlank(message = "FirstName is mandatory")
    @Size(min = 3, max = 20, message = "Firstname must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z]+( [a-zA-Z]+)*$", message = "FirstName must only contain letters and optional spaces between words")
    private String firstname;


    @NotBlank(message = "FirstName is mandatory")
    @Size(min = 3, max = 20, message = "Firstname must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z]+( [a-zA-Z]+)*$", message = "lastName must only contain letters and optional spaces between words")
    private String lastname;


    @NotBlank(message = "Username is mandatory")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$",
            message = "Password must contain at least one letter and one number")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phonenumber;

}
