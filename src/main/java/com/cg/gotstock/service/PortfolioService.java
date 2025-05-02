
package com.cg.gotstock.service;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private ExternalApiService externalApiService;


    public void addStock( StockHoldingDTO stockHoldingDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null){
            throw new RuntimeException("User not found");
        }
        StockHolding holding = new StockHolding();
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());
        holding.setCurrentPrice(externalApiService.fetchStockData(stockHoldingDTO.getSymbol()));

        holding.setUser(user);
        stockHoldingRepository.save(holding);
        log.info("stock added");
    }

    public List<StockHoldingDTO> getAllStocks() {
        log.info("in getAllStocks service layer");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        log.info("user found");

        Long userId=user.getId();
        if (user == null) {
            log.error("User ID {} not found", userId);
            throw new RuntimeException("User not found");
        }

        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);
        return holdings.stream().map(holding -> {
            StockHoldingDTO dto = new StockHoldingDTO();
            dto.setId(holding.getId());
            dto.setSymbol(holding.getSymbol());
            dto.setQuantity(holding.getQuantity());
            dto.setPurchasePrice(holding.getPurchasePrice());
            dto.setCurrentPrice(holding.getCurrentPrice());
            return dto;
        }).collect(Collectors.toList());
    }


    public void removeStock(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found: {}", user.getUsername());
            throw new RuntimeException("User not found");
        }
        StockHolding holding = stockHoldingRepository.findById(id).orElse(null);

        stockHoldingRepository.delete(holding);
        log.info("Stock holding ID {} removed for user: {}", id, user.getUsername());
    }


    public void updateStock(Long id, StockHoldingDTO stockHoldingDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
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
        holding.setCurrentPrice(externalApiService.fetchStockData(stockHoldingDTO.getSymbol()));
        stockHoldingRepository.save(holding);
    }

//    public void generateSummary()
}

