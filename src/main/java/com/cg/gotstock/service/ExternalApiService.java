package com.cg.gotstock.service;


//import com.bridgelabz.employeepayroll.dto.ResponseDTO;
import com.cg.gotstock.model.StockHolding;
import com.google.gson.Gson;
import com.cg.gotstock.dto.PortfolioResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class ExternalApiService {
    class Value {
        String datetime;
        String close;
    }

    class StockResponse {
        List<Value> values;
        String status;
    }

    private final RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;
    // Inject RestTemplate through constructor
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchStockData(String symbol) {

//        String apiKey = ${API_KEY};
        String apiKey = "71fb84fc23244c0da6f36ba08e70ebcc";
        String interval = "5min"; // Or use 5min, 15min, 1day, etc.

        // Build the URL
        String url = "https://api.twelvedata.com/time_series?symbol=" + symbol +
                "&interval=" + interval + "&apikey=" + apiKey;


        // Make the GET request
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Return the raw JSON as String
       // return response.getBody();

        Gson gson = new Gson();
        StockResponse stockResponse = gson.fromJson(response.getBody(), StockResponse.class);
        if (stockResponse.values != null && !stockResponse.values.isEmpty()) {
            Value latest = stockResponse.values.get(0);

            double priceRounded = new java.math.BigDecimal(latest.close)
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();

            return symbol.toUpperCase() + " Current Price: $" + priceRounded + " (as of " + latest.datetime + ")";
        } else {
            return "No stock data available.";
        }

    }

    @Scheduled(fixedRate = 300000) // 300,000 milliseconds = 5 minutes
    public PortfolioResponseDTO sendStockDataEmail() {
        String stockData = fetchStockData("AAPL");

        // Compose Email
        emailService.sendEmail("ryan79kumar@gmail.com","Live Stock Data", stockData);
        return  new PortfolioResponseDTO("Send Live Stock Data",null);
    }
}