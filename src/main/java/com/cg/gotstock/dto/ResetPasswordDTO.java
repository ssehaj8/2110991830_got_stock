package com.cg.gotstock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank
    private String email;

    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 3, max = 20, message = "New Password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$",
            message = "Password must contain at least one letter and one number")
    private String newPassword;
}
