package com.cg.gotstock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockHoldingDTO {

    private Long id;

    @NotBlank(message = "Stock symbol is mandatory")
    private String symbol;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Purchase price is mandatory")
    @Min(value = 0, message = "Purchase price cannot be negative")
    private Double purchasePrice;

    private Double currentPrice;

    private Double gainLoss;


}