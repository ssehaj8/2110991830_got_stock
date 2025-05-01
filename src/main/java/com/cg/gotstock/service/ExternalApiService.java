package com.cg.gotstock.service;


//import com.bridgelabz.employeepayroll.dto.ResponseDTO;
import com.cg.gotstock.dto.PortfolioResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;
    // Inject RestTemplate through constructor
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchStockData(String symbol) {
        String apiKey = "D15MJ7E4K143KZFY";
        // String symbol = "AXISBANK";
        String interval = "5min";

        // Build the URL
        String url = "https://www.alphavantage.co/query" +
                "?function=TIME_SERIES_INTRADAY" +
                "&symbol=" + symbol +
                "&interval=" + interval +
                "&apikey=" + apiKey;

        // Make the GET request
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Return the raw JSON as String
        return response.getBody();
    }

    @Scheduled(fixedRate = 300000) // 300,000 milliseconds = 5 minutes
    public PortfolioResponseDTO sendStockDataEmail() {
        String stockData = fetchStockData("AAPL");

        // Compose Email
        emailService.sendEmail("ryan79kumar@gmail.com","Live Stock Data", stockData);
        return  new PortfolioResponseDTO("Send Live Stock Data",null);
    }
}