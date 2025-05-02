package com.cg.gotstock.repository;

import com.cg.gotstock.model.StockHolding;

import com.cg.gotstock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByUser(User user);
    List<StockHolding> findByUserId(Long userId);

    // No extra methods needed for this delete logic
    }

