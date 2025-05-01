package com.cg.gotstock.repository;

import com.cg.gotstock.model.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findBySymbolIgnoreCase(String symbol);
}
