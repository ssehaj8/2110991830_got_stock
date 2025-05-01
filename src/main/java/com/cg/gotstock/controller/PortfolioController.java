package com.cg.gotstock.controller;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/add")
    public ResponseEntity<?> addStock(@Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        portfolioService.addStock(stockHoldingDTO.getUsername(), stockHoldingDTO);
        return ResponseEntity.ok("Stock added successfully");
}

}
