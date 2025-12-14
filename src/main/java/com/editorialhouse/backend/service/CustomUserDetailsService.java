package com.editorialhouse.backend.service;

import com.editorialhouse.backend.model.Privilege;
import com.editorialhouse.backend.model.Role;
import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getRoles(), user.getCustomPrivileges())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles, Collection<Privilege> customPrivileges) {

        return getGrantedAuthorities(getPrivileges(roles, customPrivileges));
    }

    private List<String> getPrivileges(Collection<Role> roles, Collection<Privilege> customPrivileges) {
        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();

        // 1. Add all privileges from roles
        for (Role role : roles) {
            collection.addAll(role.getPrivileges());
        }

        // 2. Add all custom privileges (flexible privileges)
        collection.addAll(customPrivileges);

        // 3. Convert to a unique list of privilege names
        privileges.addAll(collection.stream()
                .map(Privilege::getName)
                .collect(Collectors.toList()));

        // 4. Add role names as authorities (Spring Security best practice)
        privileges.addAll(roles.stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList()));

        return privileges.stream().distinct().collect(Collectors.toList());
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}
