package com.cg.gotstock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank
    private String email;

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
