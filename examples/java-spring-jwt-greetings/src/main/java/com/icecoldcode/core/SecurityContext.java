package com.icecoldcode.core;

import com.icecoldcode.core.authentication.internal.Principal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContext {

    public Principal requirePrincipal() {
        return (Principal) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .orElseThrow(() -> new AccessDeniedException("expected principal"));
    }

}
