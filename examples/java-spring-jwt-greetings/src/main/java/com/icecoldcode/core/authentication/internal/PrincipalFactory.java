package com.icecoldcode.core.authentication.internal;

import com.icecoldcode.core.Clock;
import com.icecoldcode.core.Entity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PrincipalFactory {

    private final Clock clock;
    @Value("${security.jwt.expiration-minutes}")
    private long jwtExpirationMinutes;

    public PrincipalFactory(Clock clock) {
        this.clock = clock;
    }

    public Principal fromAuthUser(Entity<AuthUser> authUser) {
        var now = clock.now();

        return new Principal(
                authUser.id(),
                authUser.value().getCompanyId(),
                authUser.value().getAuthorities(),
                now,
                now.plus(Duration.ofMinutes(jwtExpirationMinutes))
        );
    }

}
