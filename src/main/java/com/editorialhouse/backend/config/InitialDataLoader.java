package com.editorialhouse.backend.config;

import com.editorialhouse.backend.model.Privilege;
import com.editorialhouse.backend.model.Role;
import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.repository.PrivilegeRepository;
import com.editorialhouse.backend.repository.RoleRepository;
import com.editorialhouse.backend.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepository, RoleRepository roleRepository, PrivilegeRepository privilegeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        // == 1. Create Privileges
        Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        Privilege publishPrivilege = createPrivilegeIfNotFound("PUBLISH_PRIVILEGE");
        Privilege approvePrivilege = createPrivilegeIfNotFound("APPROVE_ARTICLE_PRIVILEGE");
        Privilege manageUsersPrivilege = createPrivilegeIfNotFound("MANAGE_USERS_PRIVILEGE");
        Privilege deleteAnyArticlePrivilege = createPrivilegeIfNotFound("DELETE_ANY_ARTICLE_PRIVILEGE");
        Privilege nominateModeratorPrivilege = createPrivilegeIfNotFound("NOMINATE_MODERATOR_PRIVILEGE");
        Privilege reportNewsPrivilege = createPrivilegeIfNotFound("REPORT_NEWS_PRIVILEGE");
        Privilege reviewReportPrivilege = createPrivilegeIfNotFound("REVIEW_REPORT_PRIVILEGE");
        Privilege grantRevokePrivilege = createPrivilegeIfNotFound("GRANT_REVOKE_PRIVILEGE"); // For flexible privileges

        // == 2. Create Roles and assign default Privileges
        Collection<Privilege> adminPrivileges = new HashSet<>(Arrays.asList(
                readPrivilege, writePrivilege, publishPrivilege, approvePrivilege, manageUsersPrivilege,
                deleteAnyArticlePrivilege, nominateModeratorPrivilege, reviewReportPrivilege, grantRevokePrivilege));

        Collection<Privilege> supervisorPrivileges = new HashSet<>(Arrays.asList(
                readPrivilege, approvePrivilege, deleteAnyArticlePrivilege, nominateModeratorPrivilege, reviewReportPrivilege));

        Collection<Privilege> clerkPrivileges = new HashSet<>(Arrays.asList(
                readPrivilege, writePrivilege, publishPrivilege));

        Collection<Privilege> userPrivileges = new HashSet<>(Arrays.asList(
                readPrivilege, reportNewsPrivilege));


        createRoleIfNotFound("ADMIN", adminPrivileges);
        createRoleIfNotFound("SUPERVISOR", supervisorPrivileges);
        createRoleIfNotFound("CLERK", clerkPrivileges);
        createRoleIfNotFound("USER", userPrivileges);

        // == 3. Create initial users
        Role adminRole = roleRepository.findByName("ADMIN");
        Role clerkRole = roleRepository.findByName("CLERK");

        // Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("adminpass"),
                    "Super Admin",
                    "System Administrator",
                    "default_admin.png",
                    new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))
            );
            admin.setRoles(Set.of(adminRole));
            admin.setCustomPrivileges(Set.of()); // No custom privileges needed for Admin by default
            userRepository.save(admin);
        }

        // Clerk User
        if (userRepository.findByUsername("clerk").isEmpty()) {
            User clerk = new User(
                    "clerk",
                    passwordEncoder.encode("clerkpass"),
                    "Clerk Joe",
                    "Junior Journalist",
                    "default_clerk.png",
                    new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))
            );
            clerk.setRoles(Set.of(clerkRole));
            // Example of flexible privilege: giving a Clerk the power to review reports
            clerk.setCustomPrivileges(Set.of(reviewReportPrivilege));
            userRepository.save(clerk);
        }

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {
        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    void createRoleIfNotFound(String name, Collection<Privilege> privileges) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
    }
}
