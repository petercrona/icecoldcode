package com.icecoldcode.core.authentication.internal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AuthUser implements UserDetails {

    private static final Set<String> PERMITTED_ROLES = Set.of(
            "ROLE_ADMIN",
            "ROLE_USER"
    );

    private final String username;
    private final String password;
    private final String companyId;
    private final Set<GrantedAuthority> authorities = new HashSet<>();

    public AuthUser(String username, String password, String companyId, Set<String> authorities) {
        this.username = username;
        this.password = password;
        this.companyId = companyId;
        authorities.forEach(this::addAuthority);
    }

    public void addAuthority(String role) {
        if (PERMITTED_ROLES.contains(role)) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getCompanyId() {
        return companyId;
    }

    public boolean isAdmin() {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
