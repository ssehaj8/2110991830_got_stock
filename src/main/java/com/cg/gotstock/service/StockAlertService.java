package com.cg.gotstock.service;

import com.cg.gotstock.dto.AlertDTO;
import com.cg.gotstock.model.StockAlert;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockAlertRepository;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StockAlertService {

    @Autowired
    private StockAlertRepository stockAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    // Create a new stock alert
    public void createAlert(AlertDTO alertDTO) {
        User user = userRepository.findByUsername(alertDTO.getUsername());
        if (user == null) {
            log.error("User not found: {}", alertDTO.getUsername());
            throw new RuntimeException("User not found");
        }

        StockAlert alert = new StockAlert();
        alert.setSymbol(alertDTO.getSymbol());
        alert.setUpperThreshold(alertDTO.getUpperThreshold());
        alert.setLowerThreshold(alertDTO.getLowerThreshold());
        alert.setUser(user);
        alert.setUpperThresholdEmailSent(false);
        alert.setLowerThresholdEmailSent(false);

        stockAlertRepository.save(alert);
        log.info("Stock alert created for user: {}, symbol: {}", alertDTO.getUsername(), alertDTO.getSymbol());
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


    // Scheduled task to check all stock alerts every 30 minutes
    @Scheduled(fixedRate = 300000)
    public void checkStockAlerts() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<StockAlert> alerts = stockAlertRepository.findByUser(user);
            for (StockAlert alert : alerts) {
                checkAlertThresholds(alert, user);
            }
        }
    }

 //   @Scheduled(fixedRate = 300000)
    // Check if stock price crosses alert thresholds and send email if necessary
    private void checkAlertThresholds(StockAlert alert, User user) {
        try {
         //   Double currentPrice = calculatePortfolioValue(user.getId());
           Double currentPrice = externalApiService.fetchStockData(alert.getSymbol());
            boolean sendUpperEmail = currentPrice >= alert.getUpperThreshold() && !alert.isUpperThresholdEmailSent();
            boolean sendLowerEmail = currentPrice <= alert.getLowerThreshold() && !alert.isLowerThresholdEmailSent();

            if (sendUpperEmail) {
                emailService.sendEmail(
                        user.getEmail(),
                        "Stock Alert: Upper Threshold Reached for " + alert.getSymbol(),
                        "Dear " + user.getUsername() + ",\n\nThe stock " + alert.getSymbol() + " has reached or exceeded your upper threshold of $" + alert.getUpperThreshold() + ". Current price: $" + String.format("%.2f", currentPrice) + ".\n\nBest regards,\nGotStock Team"
                );
                alert.setUpperThresholdEmailSent(true);
                stockAlertRepository.save(alert);
                log.info("Upper threshold email sent for stock: {} to user: {}", alert.getSymbol(), user.getUsername());
            }

            if (sendLowerEmail) {
                emailService.sendEmail(
                        user.getEmail(),
                        "Stock Alert: Lower Threshold Reached for " + alert.getSymbol(),
                        "Dear " + user.getUsername() + ",\n\nThe stock " + alert.getSymbol() + " has fallen to or below your lower threshold of $" + alert.getLowerThreshold() + ". Current price: $" + String.format("%.2f", currentPrice) + ".\n\nBest regards,\nGotStock Team"
                );
                alert.setLowerThresholdEmailSent(true);
                stockAlertRepository.save(alert);
                log.info("Lower threshold email sent for stock: {} to user: {}", alert.getSymbol(), user.getUsername());
            }

            // Reset flags if price moves back within thresholds
            if (currentPrice < alert.getUpperThreshold() && alert.isUpperThresholdEmailSent()) {
                alert.setUpperThresholdEmailSent(false);
                stockAlertRepository.save(alert);
            }
            if (currentPrice > alert.getLowerThreshold() && alert.isLowerThresholdEmailSent()) {
                alert.setLowerThresholdEmailSent(false);
                stockAlertRepository.save(alert);
            }
        } catch (MessagingException e) {
            log.error("Failed to send email for stock alert {} to user {}: {}", alert.getSymbol(), user.getUsername(), e.getMessage());
        } catch (Exception e) {
            log.error("Error checking stock alert for {}: {}", alert.getSymbol(), e.getMessage());
        }
    }
}