package com.icecoldcode.core.authentication.internal.infrastructure;

import com.icecoldcode.core.Clock;
import com.icecoldcode.core.authentication.internal.JwtService;
import com.icecoldcode.core.authentication.internal.Principal;
import com.icecoldcode.core.authentication.internal.PrincipalFactory;
import com.icecoldcode.core.authentication.internal.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> ROOTS_TO_RUN_ON = Set.of(
            "api"
    );
    private final JwtService jwtService;
    private final Clock clock;
    private final UserRepository userRepository;
    private final PrincipalFactory principalFactory;
    @Value("${security.jwt.renew-minutes}")
    private long jwtRenewMinutes;

    JwtAuthenticationFilter(
            JwtService jwtService,
            Clock clock,
            UserRepository userRepository,
            PrincipalFactory principalFactory
    ) {
        this.jwtService = jwtService;
        this.clock = clock;
        this.userRepository = userRepository;
        this.principalFactory = principalFactory;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication() != null
                || request.getCookies() == null
                || !ROOTS_TO_RUN_ON.contains(getRoot(request));
    }

    private String getRoot(@NonNull HttpServletRequest request) {
        var parts = request.getRequestURI().split("/");
        return parts.length > 1 ? parts[1] : "";
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        jwtService.principalFromCookie(request.getCookies())
                .ifPresent(principal -> {
                    SecurityContextHolder.getContext().setAuthentication(
                            new PreAuthenticatedAuthenticationToken(
                                    principal,
                                    null,
                                    principal.authorities()
                            )
                    );

                    if (issuedLongAgo(principal)) {
                        userRepository.getById(principal.userId())
                                .map(principalFactory::fromAuthUser)
                                .map(jwtService::generateJwtCookie)
                                .ifPresentOrElse(
                                        response::addCookie,
                                        () -> {
                                            // Could not load user!
                                            response.addCookie(jwtService.clearJwtCookie());
                                            SecurityContextHolder.clearContext();
                                        }
                                );
                    } else {
                        response.addCookie(jwtService.generateJwtCookieWithNewExpires(principal));
                    }

                });

        filterChain.doFilter(request, response);
    }

    private boolean issuedLongAgo(Principal principal) {
        return principal.issuedAt().isBefore(
                clock.now().minus(Duration.ofMinutes(jwtRenewMinutes))
        );
    }

}
