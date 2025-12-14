package com.editorialhouse.backend.controller;

import com.editorialhouse.backend.model.Privilege;
import com.editorialhouse.backend.model.Role;
import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.repository.PrivilegeRepository;
import com.editorialhouse.backend.repository.RoleRepository;
import com.editorialhouse.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('MANAGE_USERS_PRIVILEGE')") // Only Admin can manage users by default
public class UserManagementController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public UserManagementController(UserRepository userRepository, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<User> updateRoles(@PathVariable Long userId, @RequestBody List<String> roleNames) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> newRoles = roleNames.stream()
                .map(roleRepository::findByName)
                .collect(Collectors.toSet());
        user.setRoles(newRoles);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Admin can escalate or deescalate other user's privileges (GRANT_REVOKE_PRIVILEGE)
    @PreAuthorize("hasAuthority('GRANT_REVOKE_PRIVILEGE')")
    @PutMapping("/{userId}/privileges")
    public ResponseEntity<User> updateCustomPrivileges(@PathVariable Long userId, @RequestBody List<String> privilegeNames) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Privilege> newPrivileges = privilegeNames.stream()
                .map(privilegeRepository::findByName)
                .collect(Collectors.toSet());
        user.setCustomPrivileges(newPrivileges);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/privileges")
    public ResponseEntity<List<Privilege>> getAllPrivileges() {
        return ResponseEntity.ok(privilegeRepository.findAll());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
}
