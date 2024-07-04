package com.icecoldcode.core.authentication.internal;

import com.icecoldcode.core.Entity;

import java.util.Optional;

public interface UserRepository {
    Optional<Entity<AuthUser>> getById(long id);

    Optional<Entity<AuthUser>> getByEmail(String email);

    long save(AuthUser authUser);
}
