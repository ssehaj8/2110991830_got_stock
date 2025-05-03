package com.cg.gotstock.controller;

import com.cg.gotstock.dto.AlertDTO;
import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.service.PortfolioService;
import com.cg.gotstock.service.StockAlertService;
import com.lowagie.text.DocumentException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for handling stock portfolio operations.
 */
@RestController
@Slf4j
public class PortfolioController {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private PortfolioService portfolioService;

    /**
     * Adds a new stock to the portfolio.
     *
     * @param stockHoldingDTO DTO containing stock details
     * @return ResponseEntity with status and message
     */

    @Autowired
    private StockAlertService stockAlertService;

    @PostMapping("/add")
    public ResponseEntity<?> addStock(@Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        return portfolioService.addStock(stockHoldingDTO);
    }

    /**
     * Retrieves all stocks in the portfolio.
     *
     * @return ResponseEntity containing list of stocks
     */
    @GetMapping("/get-stock")
    public ResponseEntity<?> getAllStocks() {
        log.info("Fetching all stocks from portfolio");
        return ResponseEntity.ok(portfolioService.getAllStocks());
    }

    /**
     * Deletes a stock based on ID.
     *
     * @param id ID of the stock to delete
     * @return ResponseEntity confirming deletion
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStock(@RequestParam Long id) {
        portfolioService.removeStock(id);
        return ResponseEntity.ok("Stock deleted successfully");
    }

    /**
     * Updates a stock's information based on ID.
     *
     * @param id               ID of the stock to update
     * @param stockHoldingDTO  Updated stock data
     * @return ResponseEntity with update status
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateStock(
            @PathVariable(value = "id") Long id,
            @Valid @RequestBody StockHoldingDTO stockHoldingDTO) {
        return portfolioService.updateStock(id, stockHoldingDTO);
    }

    /**
     * Sends the stock report via email.
     *
     * @return ResponseEntity confirming report dispatch
     * @throws MessagingException    if email fails
     * @throws DocumentException     if report generation fails
     */
    @PostMapping("/report")
    public ResponseEntity<?> sendReport() throws MessagingException, DocumentException {
        portfolioService.sendStockReport();
        return ResponseEntity.ok("Report sent successfully");
    }



    @PostMapping("/alert")
    public ResponseEntity<?> createAlert(@Valid @RequestBody AlertDTO alertDTO) {
        log.info("Creating stock alert for symbol: {}", alertDTO.getSymbol());
            stockAlertService.createAlert(alertDTO);
        return ResponseEntity.ok("Stock alert sent successfully");

    }
}
