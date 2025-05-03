package com.cg.gotstock.service;

import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.google.gson.Gson;
import com.cg.gotstock.dto.PortfolioResponseDTO;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class to fetch stock data from an external API and send emails with stock information.
 */
@Service
public class ExternalApiService {

    // Logger instance for logging actions in this class
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);

    // Inner class to map response from the external API
    class Value {
        String datetime;
        String close;
    }

    // Inner class to map the response containing stock values and status
    class StockResponse {
        List<Value> values;
        String status;
    }

    private final RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    /**
     * Constructor injection for RestTemplate to interact with external APIs.
     *
     * @param restTemplate RestTemplate instance
     */
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the latest stock data for a given stock symbol using an external API.
     *
     * @param symbol the stock symbol to fetch data for
     * @return the rounded stock price
     */
    public Double fetchStockData(String symbol) {
        String apiKey = "71fb84fc23244c0da6f36ba08e70ebcc"; // API Key for the stock data service
        String interval = "5min"; // The time interval for stock data (e.g., 5min, 15min, 1day)

        // Build the URL to call the external API for stock data
        String url = "https://api.twelvedata.com/time_series?symbol=" + symbol +
                "&interval=" + interval + "&apikey=" + apiKey;

        try {
            // Make the GET request to fetch stock data
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("Stock data fetched successfully for symbol: {}", symbol);

            // Parse the JSON response using Gson
            Gson gson = new Gson();
            StockResponse stockResponse = gson.fromJson(response.getBody(), StockResponse.class);

            double priceRounded = 0;
            // Check if the response contains valid stock data
            if (stockResponse.values != null && !stockResponse.values.isEmpty()) {
                // Extract the latest stock data and round the price to 2 decimal places
                Value latest = stockResponse.values.get(0);
                priceRounded = new BigDecimal(latest.close)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                logger.info("Latest stock price for {}: {}", symbol, priceRounded);
            } else {
                logger.error("No stock data available for symbol: {}", symbol);
            }
            return priceRounded;
        } catch (Exception e) {
            // Log error if the API request fails
            logger.error("Error fetching stock data for symbol: {}. Error: {}", symbol, e.getMessage());
            return 0.0;
        }
    }

    // Uncomment and use if you want to schedule stock data email sending every 5 minutes
    /*
    @Scheduled(fixedRate = 300000) // 300,000 milliseconds = 5 minutes
    public void sendStockDataEmail() throws MessagingException {
        // Fetch stock data for a given symbol, e.g., AAPL (Apple)
        Double stockData = fetchStockData("AAPL");

        if (stockData != 0.0) {
            // Compose and send the stock data email
            emailService.sendEmail("ryan79kumar@gmail.com", "Live Stock Data", stockData);
            logger.info("Sent live stock data email successfully.");
        } else {
            logger.warn("Failed to fetch stock data. No email sent.");
        }
    }
    */
}
