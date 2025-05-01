package com.cg.gotstock.service;

import com.cg.gotstock.dto.UserLoginDTO;
import com.cg.gotstock.dto.UserRegisterDTO;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<User> registerUser(UserRegisterDTO registerDTO) {
        User user=new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    public String loginUser(UserLoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername());
        if (user == null || !loginDTO.getPassword().equals(user.getPassword()) ) {
            throw new RuntimeException("Invalid credentials");
        }
        return "user logged in successfully";
    }
}
