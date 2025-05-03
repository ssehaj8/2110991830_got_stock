package com.cg.gotstock.security;

import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service to load user details for authentication using email.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user details by email for authentication purposes.
     *
     * @param email the email of the user
     * @return UserDetails containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Retrieve user from the repository using email
        User user = userRepository.findByEmail(email);

        // Check if user exists and return user details, else throw exception
        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())  // Using email as the username
                    .password(user.getPassword())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
