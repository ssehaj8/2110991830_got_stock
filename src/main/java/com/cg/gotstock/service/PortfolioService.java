package com.cg.gotstock.service;

import com.cg.gotstock.dto.GetStockResponse;
import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import com.cg.gotstock.utility.PdfGenerator;
import com.lowagie.text.DocumentException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortfolioService {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private PdfGenerator pdfGenerator;

    @Autowired
    private EmailService emailService;

    /**
     * Adds a stock holding to the user's portfolio.
     *
     * @param stockHoldingDTO DTO containing stock holding details.
     * @return ResponseEntity with updated StockHoldingDTO.
     */
    public ResponseEntity<?> addStock(StockHoldingDTO stockHoldingDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Fetch the user from the database based on email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User with email {} not found", email);
            throw new RuntimeException("User not found");
        }

        // Create and populate a new stock holding
        StockHolding holding = new StockHolding();
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());
        holding.setCurrentPrice(externalApiService.fetchStockData(stockHoldingDTO.getSymbol()));

        // Calculate the gain/loss for the stock holding
        Double value = stockHoldingDTO.getQuantity() * (externalApiService.fetchStockData(stockHoldingDTO.getSymbol()) - stockHoldingDTO.getPurchasePrice());
        holding.setGainLoss(value);
        holding.setUser(user);

        // Save the stock holding in the repository
        stockHoldingRepository.save(holding);

        // Update the DTO with stock holding details
        stockHoldingDTO.setId(holding.getId());
        stockHoldingDTO.setCurrentPrice(holding.getCurrentPrice());
        stockHoldingDTO.setGainLoss(holding.getGainLoss());

        log.info("Stock with symbol {} added to portfolio", stockHoldingDTO.getSymbol());
        return new ResponseEntity<>(stockHoldingDTO, HttpStatus.OK);
    }

    /**
     * Retrieves all stocks in the user's portfolio.
     *
     * @return ResponseEntity with the list of all stocks and total portfolio value.
     */
    public ResponseEntity<GetStockResponse> getAllStocks() {
        log.info("Fetching all stocks in portfolio");

        // Fetch the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        log.info("user found");

        Long userId=user.getId();
        if (user == null) {
            log.error("User with email {} not found", email);
            throw new RuntimeException("User not found");
        }

       // Long userId = user.getId();
        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);

        // Calculate the total portfolio value
        Double totalPortfolioValue = 0.0;
        for (StockHolding holding : holdings) {
            totalPortfolioValue += holding.getGainLoss();
        }

        // Map the stock holdings to DTOs
        List<StockHoldingDTO> dtoList = new ArrayList<>();
        for (StockHolding holding : holdings) {
            StockHoldingDTO dto = new StockHoldingDTO();
            dto.setId(holding.getId());
            dto.setSymbol(holding.getSymbol());
            dto.setQuantity(holding.getQuantity());
            dto.setPurchasePrice(holding.getPurchasePrice());
            dto.setCurrentPrice(holding.getCurrentPrice());
            dto.setGainLoss(holding.getGainLoss());
            dtoList.add(dto);
        }

        // Create response DTO with stock data and portfolio value
        GetStockResponse response = new GetStockResponse();
        response.setList(dtoList);
        response.setTotalPortfolioValue(totalPortfolioValue);

        log.info("Total portfolio value calculated: {}", totalPortfolioValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Removes a stock holding from the user's portfolio by its ID.
     *
     * @param id the ID of the stock holding to remove.
     */
    public void removeStock(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User with email {} not found", email);
            throw new RuntimeException("User not found");
        }

        StockHolding holding = stockHoldingRepository.findById(id).orElse(null);
        if (holding == null) {
            log.error("Stock holding with ID {} not found", id);
            throw new RuntimeException("Stock not found");
        }

        // Delete the stock holding from the repository
        stockHoldingRepository.delete(holding);
        log.info("Stock holding with ID {} removed from user {}'s portfolio", id, user.getUsername());
    }

    /**
     * Updates a stock holding in the user's portfolio.
     *
     * @param id the ID of the stock holding to update.
     * @param stockHoldingDTO DTO containing updated stock details.
     * @return ResponseEntity with updated StockHoldingDTO.
     */
    public ResponseEntity<?> updateStock(Long id, StockHoldingDTO stockHoldingDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User with email {} not found", email);
            throw new RuntimeException("User not found");
        }

        StockHolding holding = stockHoldingRepository.findById(id).orElse(null);
        if (holding == null) {
            log.error("Stock holding with ID {} not found", id);
            throw new RuntimeException("Stock not found");
        }

        // Ensure the stock holding belongs to the current user
        if (holding.getUser() != user) {
            log.error("Stock holding with ID {} does not belong to the user {}", id, user.getUsername());
            throw new RuntimeException("User not in stock");
        }

        // Update the stock holding details
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());
        holding.setCurrentPrice(externalApiService.fetchStockData(stockHoldingDTO.getSymbol()));
        Double value = stockHoldingDTO.getQuantity() * (externalApiService.fetchStockData(stockHoldingDTO.getSymbol()) - stockHoldingDTO.getPurchasePrice());
        holding.setGainLoss(value);
        holding.setUser(user);

        // Save the updated stock holding
        stockHoldingRepository.save(holding);

        // Update the DTO with the new values
        stockHoldingDTO.setId(holding.getId());
        stockHoldingDTO.setCurrentPrice(holding.getCurrentPrice());
        stockHoldingDTO.setGainLoss(holding.getGainLoss());

        log.info("Stock holding with ID {} updated for user {}", id, user.getUsername());
        return new ResponseEntity<>(stockHoldingDTO, HttpStatus.OK);
    }

    /**
     * Generates a PDF report of the user's stock holdings and sends it via email.
     *
     * @throws MessagingException if email sending fails.
     * @throws DocumentException if PDF generation fails.
     */
    public void sendStockReport() throws MessagingException, DocumentException {
        log.info("Generating stock report");

        // Fetch all stock holdings and calculate total portfolio value
        ResponseEntity<GetStockResponse> response = getAllStocks();
        List<StockHoldingDTO> holdings = response.getBody().getList();
        Float totalPortfolioValue = response.getBody().getTotalPortfolioValue().floatValue();

        // Fetch the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User with email {} not found", email);
            throw new RuntimeException("User not found");
        }

        // Generate PDF report
        byte[] pdfBytes = pdfGenerator.generatePDF(holdings, totalPortfolioValue, user.getUsername());
        log.info("PDF report generated successfully for {}", user.getFirstname() + " " + user.getLastname());

        // Send the email with the PDF attachment
        emailService.sendEmailWithAttachment(
                email,
                "Your Stock holdings attached.",
                "Please find your stock holdings attached.",
                pdfBytes,
                "Stock_holdings_report.pdf"
        );
        log.info("Stock report email sent to: {}", email);
    }
}
