package com.cg.gotstock.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Double upperThreshold;

    private Double lowerThreshold;

    private boolean upperThresholdEmailSent;

    private boolean lowerThresholdEmailSent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}