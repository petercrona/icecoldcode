package com.icecoldcode.core.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public record CreateAuthUserDto(String username,
                                String password,
                                String companyId,
                                Set<String> roles) {
    @JsonCreator
    public CreateAuthUserDto(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("companyId") String companyId,
            @JsonProperty("roles") Set<String> roles) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.companyId = Objects.requireNonNull(companyId);
        this.roles = Objects.requireNonNullElse(roles, Set.of());
    }
}