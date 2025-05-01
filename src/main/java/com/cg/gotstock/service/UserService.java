package com.cg.gotstock.service;

import com.cg.gotstock.dto.PortfolioResponseDTO;
import com.cg.gotstock.dto.UserLoginDTO;
import com.cg.gotstock.dto.UserRegisterDTO;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import com.cg.gotstock.utility.JwtUtility;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
public class UserService implements UserInterface{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtUtility jwtUtility;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public PortfolioResponseDTO<String, String >registerUser(UserRegisterDTO registerDTO) {
        log.info("Registering user: {}", registerDTO.getEmail());
        PortfolioResponseDTO<String,String> res= new PortfolioResponseDTO<>();
        if(existsByEmail(registerDTO.getEmail())){
            log.warn("Registration failed: User already exists with email {}", registerDTO.getEmail());
            res.setMessage("error");
            res.setData("User Already Exists");
            return res;
        }
        User user=new User();
        user.setFirstname(registerDTO.getFirstname());
        user.setLastname(registerDTO.getLastname());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhonenumber(registerDTO.getPhonenumber());
        user.setPassword(registerDTO.getPassword());
        userRepository.save(user);

        log.info("User {} registered successfully!", user.getEmail());
        res.setMessage("message");
        res.setData("User Registed Successfully");
        return res;
    }

    public PortfolioResponseDTO<String, String> loginUser(UserLoginDTO loginDTO) throws MessagingException {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        //User user = userRepository.findByUsername(loginDTO.getUsername());
        PortfolioResponseDTO <String, String>res = new PortfolioResponseDTO<>();
        Optional<User> userExists = getUserByEmail(loginDTO.getEmail());

        if (userExists.isPresent()) {
            User user = userExists.get();
            if (matchPassword(loginDTO.getPassword(), user.getPassword())) {
                String token = jwtUtility.generateToken(user.getEmail());
                user.setToken(token);
                userRepository.save(user);
                log.debug("Login successful for user: {}- Token generated", user.getEmail());
                    emailService.sendEmail(user.getEmail(), "Logged in Employee Payroll App", "Hii"
                        + "\n You have been successfully logged in!" + token);
                res.setMessage("message");
                res.setData("User Logged In Successfully: " + token);
                return res;

            } else {
                log.warn("Invalid credentials for user: {}", loginDTO.getEmail());
                res.setMessage("error");
                res.setData("Invalid Credentials");
                return res;
            }

        } else {
            log.error("User not found with email: {}", loginDTO.getEmail());
            res.setMessage("error");
            res.setData("User Not Found");
            return res;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.findByUsername(email).isPresent();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
}
