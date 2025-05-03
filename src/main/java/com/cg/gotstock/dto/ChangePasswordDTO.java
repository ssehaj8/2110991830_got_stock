package com.cg.gotstock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank
    @Size(min=3, max = 20, message = "new password cannot be empty")
    private String newPassword;

    @NotBlank
    private String currentPassword;
}
