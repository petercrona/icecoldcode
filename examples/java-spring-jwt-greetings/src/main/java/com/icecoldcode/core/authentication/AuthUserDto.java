package com.icecoldcode.core.authentication;

import java.util.Objects;

record AuthUserDto(String username, String companyId, boolean isAdmin) {
    public AuthUserDto {
        Objects.requireNonNull(username);
        Objects.requireNonNull(companyId);
    }
}