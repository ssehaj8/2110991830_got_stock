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

@Component
public class JwtUtility {
    @Autowired
    UserRepository userRepository;
    private static final String SECRET_KEY="ABCDEFGHIJKLMNOP123456789ABCDEHI*";

    public String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+2*60*1000*60))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractEmail(String token){
        try {
            System.out.println(token);
            Claims claims=Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("Getting email => "+claims);
            return claims.getSubject();
        }
        catch (Exception e){
            //Token expired
            return e.getMessage();

        }
    }
    public boolean validateJwt(String token, String userEmail){
        final String email= extractEmail(token);
        boolean isTokenPresent= true;
        User user = userRepository.findByEmail(email);
//        if(user.isPresent() && user.getToken()==null){
//            isTokenPresent=false;
//        }
        final boolean valid= Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody().getExpiration().before(new Date());

        return (email.equals(userEmail) && !valid && isTokenPresent);
    }
}
