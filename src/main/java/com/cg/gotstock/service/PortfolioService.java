package com.cg.gotstock.service;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import com.lowagie.text.DocumentException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private  ExternalApiService externalApiService;

    @Autowired
    private PdfGenerator pdfGenerator;

    @Autowired
    private EmailService emailService;

    public void addStock(String username, StockHoldingDTO stockHoldingDTO) {
        User user = userRepository.findByUsername(username);
        if (user == null){
            throw new RuntimeException("User not found");
        }
        StockHolding holding = new StockHolding();
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());

       holding.setCurrentPrice(externalApiService.fetchStockData(stockHoldingDTO.getSymbol()));
  //      holding.setCurrentPrice(stockHoldingDTO.getCurrentPrice());
        holding.setUser(user);
        stockHoldingRepository.save(holding);
        log.info("stock added");
    }

    public List<StockHoldingDTO> getAllStocks(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User ID {} not found", userId);
            throw new RuntimeException("User not found");
        }

        List<StockHolding> holdingList = stockHoldingRepository.findByUserId(userId);
        List<StockHoldingDTO> dtoList = new ArrayList<>();

        for (StockHolding holding : holdingList) {
            StockHoldingDTO dto = new StockHoldingDTO();
            dto.setSymbol(holding.getSymbol());
            dto.setQuantity(holding.getQuantity());
            dto.setPurchasePrice(holding.getPurchasePrice());
            dto.setCurrentPrice(holding.getCurrentPrice());
            dto.setUsername(user.getUsername());
            dtoList.add(dto);
        }

        return dtoList;
    }



    public void removeStock(String username, Long id) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found: {}", username);
            throw new RuntimeException("User not found");
        }

        StockHolding holding = stockHoldingRepository.findById(id).orElse(null);

        stockHoldingRepository.delete(holding);
        log.info("Stock holding ID {} removed for user: {}", id, username);
    }


    public void updateStock(String username, Long id, StockHoldingDTO stockHoldingDTO) {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User not found");
        }
        StockHolding holding=stockHoldingRepository.findById(id).orElse(null);
        if(holding==null){
            throw new RuntimeException("Stock not found");
        }
        if(holding.getUser()!=user){
            throw new RuntimeException("User not in stock");
        }
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());
//        holding.setCurrentPrice(stockPriceService.getCurrentPrice(stockHoldingDTO.getSymbol()));
        holding.setCurrentPrice(stockHoldingDTO.getCurrentPrice());
        stockHoldingRepository.save(holding);
    }

    public void sendStockReport(Long userId, String email) throws MessagingException, DocumentException {
        log.info("Generating stock report for user ID: {}", userId);
        List<StockHoldingDTO> holdings = getAllStocks(userId);
        User user = userRepository.findById(userId).orElse(null);

        if(user==null){
            throw new RuntimeException("User not found");
        }

        byte[] pdfBytes = pdfGenerator.generatePDF(holdings, user.getUsername());
        log.info("PDF generated successfully for user: {}", user.getUsername());

        emailService.sendEmailWithAttachment(
                email,
                "Your Stock holdings attatched.",
                "Please find your stock holdings attached.",
                pdfBytes,
                "Stock_holdings_report.pdf"
        );
        log.info("Stock report email sent to: {}", email);
    }
}
