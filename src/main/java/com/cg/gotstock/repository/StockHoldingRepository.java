package com.cg.gotstock.repository;

import com.cg.gotstock.model.StockHolding;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByUser(User user);
}
