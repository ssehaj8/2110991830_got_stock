package com.cg.gotstock.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortfolioResponseDTO {

    private Long id;
    private String username;
    private List<StockHoldingDTO> holdings;
    private Double totalValue;
    private Double totalGainLoss;
    private Double totalGainLossPercentage;
}