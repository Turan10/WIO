package app.wio.service;

import app.wio.entity.User;
import app.wio.repository.UserRepository;
import app.wio.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return buildUserDetails(user);
    }


    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        logger.info("Loading user by ID: {}", id);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        logger.info("Found user: {}", user.getEmail());
        return buildUserDetails(user);
    }

    private CustomUserDetails buildUserDetails(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
