package com.cg.gotstock.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Integer quantity;

    private Double purchasePrice;

    private Double currentPrice;

    private Double previousPrice; // For daily change tracking

    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}