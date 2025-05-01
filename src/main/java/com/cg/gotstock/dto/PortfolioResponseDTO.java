package com.cg.gotstock.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@Data
public class PortfolioResponseDTO <S, S1>{


    private Long id;
    private String username;
    private List<StockHoldingDTO> holdings;
    private Double totalValue;
    private Double totalGainLoss;
    private Double totalGainLossPercentage;
    private String message;
    private Object data;
}