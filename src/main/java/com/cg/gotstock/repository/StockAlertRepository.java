package com.cg.gotstock.repository;

import com.cg.gotstock.model.StockAlert;
import com.cg.gotstock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByUser(User user);
}
