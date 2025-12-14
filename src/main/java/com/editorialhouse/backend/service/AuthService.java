package com.editorialhouse.backend.service;

import com.editorialhouse.backend.model.Role;
import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.repository.RoleRepository;
import com.editorialhouse.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        // Default role for new sign-ups is 'USER'
        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            throw new RuntimeException("Default USER role not found!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(userRole));
        user.setCustomPrivileges(Set.of());
        user.setSessionExpirationDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
        // Set default values for required fields if not provided
        if (user.getFullName() == null) user.setFullName("New User");
        if (user.getCredentials() == null) user.setCredentials("Web User");
        if (user.getProfilePictureUrl() == null) user.setProfilePictureUrl("default_user.png");

        userRepository.save(user);
    }

    public void login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Update session expiration date (7 days)
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setSessionExpirationDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
            userRepository.save(user);
        });
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
