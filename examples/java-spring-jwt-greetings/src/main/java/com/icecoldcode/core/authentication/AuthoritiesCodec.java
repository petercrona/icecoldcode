package com.icecoldcode.core.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthoritiesCodec {

    public static Set<GrantedAuthority> fromString(String toParse) {
        Scanner scanner = new Scanner(toParse);
        Set<GrantedAuthority> authorities = new HashSet<>();
        scanner.forEachRemaining(x -> authorities.add(new SimpleGrantedAuthority(x)));
        return authorities;
    }

    public static String toString(Collection<? extends GrantedAuthority> grantedAuthorities) {
        return grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

}
