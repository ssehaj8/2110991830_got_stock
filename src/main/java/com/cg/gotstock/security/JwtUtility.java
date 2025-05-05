package com.cg.gotstock.security;

import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token generation, validation, and email extraction.
 */
@Component
public class JwtUtility {

    @Autowired
    private UserRepository userRepository;

    // Secret key for signing JWT tokens (should be kept secure)
    private static final String SECRET_KEY = "ABCDEFGHIJKLMNOP123456789ABCDEHI*";

    /**
     * Generates a JWT token for the given email.
     *
     * @param email the user's email
     * @return the generated JWT token
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 1000 * 60))  // token valid for 2 hours
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the email from a JWT token.
     *
     * @param token the JWT token
     * @return the email extracted from the token
     */
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            // In case of an error (e.g., expired token), return the error message
            return e.getMessage();
        }
    }

    /**
     * Validates the JWT token by checking the expiration and the user's email.
     *
     * @param token      the JWT token
     * @param userEmail  the email of the user trying to authenticate
     * @return true if the token is valid and matches the user email, false otherwise
     */
    public boolean validateJwt(String token, String userEmail) {
        final String email = extractEmail(token);

        // Retrieve user from the repository
        User user = userRepository.findByEmail(email);

        // Check if the token is expired
        final boolean valid = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());

        // Token is valid if the email matches and the token is not expired
        return email.equals(userEmail) && !valid;
    }
}
