package com.icecoldcode.core.authentication.internal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

public record Principal(long userId,
                        String companyId,
                        Collection<? extends GrantedAuthority> authorities,
                        Instant issuedAt,
                        Instant expiresAt) {

    public Principal {
        Objects.requireNonNull(companyId);
        Objects.requireNonNull(authorities);
        Objects.requireNonNull(issuedAt);
        Objects.requireNonNull(expiresAt);
    }

    Principal withExpiresAt(Instant newExpiresAt) {
        return new Principal(userId, companyId, authorities, issuedAt, newExpiresAt);
    }

    public boolean isAdmin() {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

}
