package com.cg.gotstock.service;

import com.cg.gotstock.dto.PortfolioResponseDTO;
import com.cg.gotstock.model.User;

import java.util.Optional;

public interface UserInterface {

    boolean matchPassword(String rawPassword, String encodedPassword);
    boolean existsByEmail(String email);
    Optional<User> getUserByEmail(String email);

}
