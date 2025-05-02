package com.cg.gotstock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertDTO {

    @NotBlank(message = "Stock symbol is mandatory")
    private String symbol;

    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotNull(message = "Upper threshold is mandatory")
    @Min(value = 0, message = "Upper threshold cannot be negative")
    private Double upperThreshold;

    @NotNull(message = "Lower threshold is mandatory")
    @Min(value = 0, message = "Lower threshold cannot be negative")
    private Double lowerThreshold;
}