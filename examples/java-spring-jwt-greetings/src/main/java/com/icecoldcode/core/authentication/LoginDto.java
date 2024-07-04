package com.icecoldcode.core.authentication;

import java.util.Objects;

public record LoginDto(String username, String password) {
    public LoginDto {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
    }
}