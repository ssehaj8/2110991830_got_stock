package com.cg.gotstock.service;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.StockHolding;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.StockHoldingRepository;
import com.cg.gotstock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortfolioService {

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private UserRepository userRepository;

    public void addStock(String username, StockHoldingDTO stockHoldingDTO) {
        User user = (User) userRepository.findByUsername(username);
        if (user == null){
            throw new RuntimeException("User not found");
        }
        StockHolding holding = new StockHolding();
        holding.setSymbol(stockHoldingDTO.getSymbol());
        holding.setQuantity(stockHoldingDTO.getQuantity());
        holding.setPurchasePrice(stockHoldingDTO.getPurchasePrice());
//        holding.setCurrentPrice(stockPriceService.getCurrentPrice(stockHoldingDTO.getSymbol()));
        holding.setCurrentPrice(stockHoldingDTO.getCurrentPrice());
        holding.setUser(user);
        stockHoldingRepository.save(holding);
        log.info("stock added");
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
}
