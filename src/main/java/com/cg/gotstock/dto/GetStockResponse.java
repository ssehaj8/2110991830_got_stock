package com.cg.gotstock.dto;

import com.cg.gotstock.model.StockHolding;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetStockResponse {

    private List<StockHoldingDTO> list=new ArrayList<>();
    private Double totalPortfolioValue;
}
