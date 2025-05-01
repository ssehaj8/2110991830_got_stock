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

    private String firstname;
    private String lastname;
    private String phonenumber;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String resetCode;

    private LocalDateTime resetCodeExpiry;
    private String token;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StockHolding> holdings;


    public boolean isPresent() {
        return false;
    }
}