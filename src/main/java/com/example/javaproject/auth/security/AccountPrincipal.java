package com.example.javaproject.auth.security;

import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal backed by an application account.
 */
public final class AccountPrincipal implements UserDetails {

    /** Persistent account identifier. */
    private final Long id;
    /** Account email used as the security username. */
    private final String email;
    /** Encoded account password. */
    private final String passwordHash;
    /** Granted application role. */
    private final UserRole role;
    /** Whether the account may authenticate. */
    private final boolean enabled;

    /**
     * Creates a security principal from a persisted user.
     *
     * @param user account represented by this principal
     */
    public AccountPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

    /** Returns the authority corresponding to the account role. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** Returns the encoded account password. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Returns the account email used for authentication. */
    @Override
    public String getUsername() {
        return email;
    }

    /** Returns whether the account is enabled. */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the persistent account identifier.
     *
     * @return account identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the account's role.
     *
     * @return assigned role
     */
    public UserRole getRole() {
        return role;
    }
}
