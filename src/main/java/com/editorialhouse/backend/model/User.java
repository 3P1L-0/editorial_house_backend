package com.editorialhouse.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "user_account") // Renamed to avoid conflict with SQL keyword 'USER'
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(unique = true, nullable = false)
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String fullName;

    @NonNull
    private String credentials; // e.g., "Senior Editor", "Journalist"

    @NonNull
    private String profilePictureUrl; // URL to the profile picture

    @NonNull
    private Date sessionExpirationDate; // For the 7-day session requirement

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    // Flexible Privileges: Granular permissions that override or extend default role-based access
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_custom_privileges",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "privilege_id", referencedColumnName = "id"))
    private Collection<Privilege> customPrivileges;

    private boolean enabled = true;
}
