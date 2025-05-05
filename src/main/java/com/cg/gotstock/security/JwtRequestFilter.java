package com.cg.gotstock.security;

import com.cg.gotstock.security.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException{

        // fetching the header value from authorization header
        final String authorizationHeader = request.getHeader("Authorization");

        // inititalizing username and token as null
        String email = null;
        String jwt = null;

        // fetching token value and username and saving it in variables
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email=jwtUtility.extractEmail(jwt);
        }

        // if security context in null then we will fetch username and validate user based on that.
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);

            // validating the token and username
            if(jwtUtility.validateJwt(jwt,email)){

                // if validation is true then build a security badge(authentication object) for user
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // adding extra details to security badge like IP, session id, session metadata, custom decisions etc.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // adding security badge to Security context holder from where spring authorize the user.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // call doFilter to tell spring that the authentication is done now other components can do their work.
        chain.doFilter(request, response);
    }
}
