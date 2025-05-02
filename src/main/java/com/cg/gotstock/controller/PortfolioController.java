package com.cg.gotstock.controller;

import com.cg.gotstock.dto.AlertDTO;
import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.service.PortfolioService;
import com.cg.gotstock.service.StockAlertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class  PortfolioController {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private PortfolioService portfolioService;


    @Autowired
    private StockAlertService stockAlertService;

    @PostMapping("/add")
    public ResponseEntity<?> addStock(@Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        portfolioService.addStock(stockHoldingDTO.getUsername(), stockHoldingDTO);
        return ResponseEntity.ok("Stock added successfully");
}

    @GetMapping("/stocks/user/{userId}")
    public ResponseEntity<?> getAllStocks(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getAllStocks(userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStock(@RequestParam String username, @RequestParam Long id) {
        portfolioService.removeStock(username, id);
        return ResponseEntity.ok("Stock deleted successfully");
    }



    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateStock(@PathVariable(value = "id") Long id, @Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        portfolioService.updateStock(stockHoldingDTO.getUsername(), id,stockHoldingDTO);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @PostMapping("/alert")
    public ResponseEntity<?> createStockAlert(@Valid @RequestBody AlertDTO alertDTO) {
        stockAlertService.createAlert(alertDTO);
        return ResponseEntity.ok("Stock alert created successfully");
    }
}
