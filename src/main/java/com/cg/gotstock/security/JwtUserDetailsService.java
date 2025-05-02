package com.cg.gotstock.security;

import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if(user != null){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail()) // Important: use email
                    .password(user.getPassword())
                    .build();
        }

        throw new UsernameNotFoundException(email);
    }

}

