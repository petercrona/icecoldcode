package com.icecoldcode.core.authentication.internal;

import com.icecoldcode.core.Clock;
import com.icecoldcode.core.authentication.AuthoritiesCodec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final String JWT_COOKIE_NAME = "jwt";
    private final Clock clock;
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-minutes}")
    private long jwtExpirationMinutes;

    public JwtService(Clock clock) {
        this.clock = clock;
    }

    public Cookie generateJwtCookie(Principal principal) {
        var jwt = Jwts
                .builder()
                .claim("cid", principal.companyId())
                .claim("authorities", AuthoritiesCodec.toString(
                        principal.authorities())
                )
                .setSubject(String.valueOf(principal.userId()))
                .setIssuedAt(Date.from(principal.issuedAt()))
                .setExpiration(Date.from(principal.expiresAt()))
                .signWith(getSigningKey())
                .compact();

        Cookie cookie = new Cookie(JWT_COOKIE_NAME, jwt);
        cookie.setMaxAge(
                (int) Duration.between(
                        clock.now(), principal.expiresAt()
                ).toSeconds()
        );
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        return cookie;
    }

    public Cookie generateJwtCookieWithNewExpires(Principal principal) {
        return generateJwtCookie(
                principal.withExpiresAt(
                        clock.now().plus(Duration.ofMinutes(jwtExpirationMinutes))
                )
        );
    }

    public Cookie clearJwtCookie() {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }

    public Optional<Principal> principalFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                return Optional.of(cookie.getValue())
                        .flatMap(this::getClaims)
                        .map(claims ->
                                new Principal(
                                        Long.parseLong(claims.getSubject()),
                                        claims.get("cid", String.class),
                                        AuthoritiesCodec.fromString(
                                                claims.get("authorities", String.class)
                                        ),
                                        claims.getIssuedAt().toInstant(),
                                        claims.getExpiration().toInstant()
                                )
                        );
            }
        }
        return Optional.empty();
    }

    private Optional<Claims> getClaims(String jwt) {
        try {
            return Optional.of(Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody());
        } catch (Exception e) {
            logger.warn("failed to get claims: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
