package com.cg.gotstock.service;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


/**
 * Service class to fetch stock data from an external API and send emails with stock information.
 */
@Service
@Slf4j
public class ExternalApiService {

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
        String apiKey = "040b895c9d0847fbaf3d434a38e04100"; // API Key for the stock data service
        String interval = "5min"; // The time interval for stock data (e.g., 5min, 15min, 1day)

        // Build the URL to call the external API for stock data
        String url = "https://api.twelvedata.com/time_series?symbol=" + symbol +
                "&interval=" + interval + "&apikey=" + apiKey;

            // Make the GET request to fetch stock data
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("Stock data fetched successfully for symbol: {}", symbol);

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
                log.info("Latest stock price for {}: {}", symbol, priceRounded);
            } else {
                log.error("No stock data available for symbol: {}", symbol);
            }
            return priceRounded;
        }
    }




