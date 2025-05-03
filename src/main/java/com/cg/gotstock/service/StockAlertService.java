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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found: {}", user.getUsername());
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
        log.info("Stock alert created for user: {}, symbol: {}", user.getUsername(), alertDTO.getSymbol());
    }

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


    /**
     * Calculates the total portfolio value for a user.
     *
     * @param userId the ID of the user.
     * @return the total portfolio value.
     */
    private Double calculatePortfolioValue(Long userId) {
        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);
        Double totalValue = 0.0;
        for (StockHolding holding : holdings) {
            double currentPrice = externalApiService.fetchStockData(holding.getSymbol());
            holding.setCurrentPrice(currentPrice);
            stockHoldingRepository.save(holding);
            totalValue += currentPrice * holding.getQuantity();
        }
        return totalValue;
    }

    /**
     * Scheduled task to check portfolio value changes every 30 minutes and send email if significant change detected.
     */
    @Scheduled(fixedRate = 300000) // 1,800,000 milliseconds = 30 minutes
    public void checkPortfolioValueChanges() {
        log.info("Checking portfolio value changes for all users");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                Double currentPortfolioValue = calculatePortfolioValue(user.getId());
                Double previousPortfolioValue = user.getPreviousPortfolioValue() != null ? user.getPreviousPortfolioValue() : 0.0;

                // Check if portfolio value changed by more than 1%
                if (previousPortfolioValue != 0.0) {
                    double changePercentage = Math.abs((currentPortfolioValue - previousPortfolioValue) / previousPortfolioValue * 100);
                    if (changePercentage >= 0.0) {
                        String subject = "Portfolio Value Change Alert";
                        String body = String.format(
                                "Dear %s,\n\nYour portfolio value has changed significantly. Previous value: $%.2f, Current value: $%.2f (%.2f%% change).\n\nBest regards,\nGotStock Team",
                                user.getUsername(), previousPortfolioValue, currentPortfolioValue, changePercentage
                        );
                        emailService.sendEmail(user.getEmail(), subject, body);
                        log.info("Portfolio value change email sent to {}: {}% change", user.getEmail(), changePercentage);
                    }
                }

                // Update previous portfolio value
                user.setPreviousPortfolioValue(currentPortfolioValue);
                userRepository.save(user);
            } catch (MessagingException e) {
                log.error("Failed to send portfolio value change email to {}: {}", user.getEmail(), e.getMessage());
            } catch (Exception e) {
                log.error("Error checking portfolio value for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }
}