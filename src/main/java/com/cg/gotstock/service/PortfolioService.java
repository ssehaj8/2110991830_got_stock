package com.cg.gotstock.service;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private EmailService emailService;


    public void addStock(String username, StockHoldingDTO stockHoldingDTO) {
        User user = (User) userRepository.findByUsername(username);
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

        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);
        return holdings.stream().map(holding -> {
            StockHoldingDTO dto = new StockHoldingDTO();
            dto.setSymbol(holding.getSymbol());
            dto.setQuantity(holding.getQuantity());
            dto.setPurchasePrice(holding.getPurchasePrice());
            dto.setCurrentPrice(holding.getCurrentPrice());
            dto.setUsername(user.getUsername());
            return dto;
        }).collect(Collectors.toList());
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
        User user=userRepository.findByUsername(username);
        if(user==null){
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


//    public double calculatePortfolioValue(Long userId) {
//        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);
//        double totalValue = 0.0;
//        for (StockHolding holding : holdings) {
//            double currentPrice = externalApiService.fetchStockData(holding.getSymbol());
//            holding.setCurrentPrice(currentPrice);
//            stockHoldingRepository.save(holding);
//            totalValue += currentPrice * holding.getQuantity();
//        }
//        return totalValue;
//    }
//
//
//    public void checkPortfolioThresholds(User user) {
//        double totalValue = calculatePortfolioValue(user.getId());
//        boolean sendHighEmail = totalValue >= 10000 && !user.isHighThresholdEmailSent();
//        boolean sendLowEmail = totalValue <= -2000 && !user.isLowThresholdEmailSent();
//
//        try {
//            if (sendHighEmail) {
//                emailService.sendEmail(
//                        user.getEmail(),
//                        "Portfolio Alert: High Value Reached",
//                        "Dear " + user.getUsername() + ",\n\nYour portfolio value has reached or exceeded $10,000. Current value: $" + String.format("%.2f", totalValue) + ".\n\nBest regards,\nGotStock Team"
//                );
//                user.setHighThresholdEmailSent(true);
//                userRepository.save(user);
//                log.info("High threshold email sent to user: {}", user.getUsername());
//            }
//            if (sendLowEmail) {
//                emailService.sendEmail(
//                        user.getEmail(),
//                        "Portfolio Alert: Low Value Reached",
//                        "Dear " + user.getUsername() + ",\n\nYour portfolio value has fallen to or below -$2,000. Current value: $" + String.format("%.2f", totalValue) + ".\n\nBest regards,\nGotStock Team"
//                );
//                user.setLowThresholdEmailSent(true);
//                userRepository.save(user);
//                log.info("Low threshold email sent to user: {}", user.getUsername());
//            }
//            // Reset flags if value moves back within normal range
//            if (totalValue < 10000 && user.isHighThresholdEmailSent()) {
//                user.setHighThresholdEmailSent(false);
//                userRepository.save(user);
//            }
//            if (totalValue > -2000 && user.isLowThresholdEmailSent()) {
//                user.setLowThresholdEmailSent(false);
//                userRepository.save(user);
//            }
//        } catch (Exception e) {
//            log.error("Failed to send email to user {}: {}", user.getUsername(), e.getMessage());
//        }
//    }
}
