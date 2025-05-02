package com.cg.gotstock.controller;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class PortfolioController {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/add")
    public ResponseEntity<?> addStock(@Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        portfolioService.addStock( stockHoldingDTO);
        return ResponseEntity.ok("Stock added successfully");
}

    @GetMapping("/get-stock")
    public ResponseEntity<?> getAllStocks() {
        log.info("in get stock controller");
        return ResponseEntity.ok(portfolioService.getAllStocks());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStock(@RequestParam Long id) {
        portfolioService.removeStock( id);
        return ResponseEntity.ok("Stock deleted successfully");
    }
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateStock(@PathVariable(value = "id") Long id, @Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        portfolioService.updateStock(id,stockHoldingDTO);
        return ResponseEntity.ok("Stock updated successfully");
    }

}
