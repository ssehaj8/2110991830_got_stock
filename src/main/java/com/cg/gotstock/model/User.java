package com.cg.gotstock.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    private String resetCode;

    private LocalDateTime resetCodeExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StockHolding> holdings;

//    private boolean highThresholdEmailSent;
//
//    private boolean lowThresholdEmailSent;
}