package com.icecoldcode.core.authentication.internal.infrastructure;

import com.icecoldcode.core.Entity;
import com.icecoldcode.core.authentication.internal.AuthUser;
import com.icecoldcode.core.authentication.internal.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
class UserRepositoryInMemory implements UserRepository {

    public static long nextId = 1;

    // Naturally, this would be a DB
    public Map<Long, Entity<AuthUser>> byId = new HashMap<>();
    public Map<String, Entity<AuthUser>> byEmail = new HashMap<>();

    @Override
    public Optional<Entity<AuthUser>> getById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Entity<AuthUser>> getByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email));
    }

    @Override
    public long save(AuthUser authUser) {
        byId.put(nextId, new Entity<>(nextId, authUser));
        byEmail.put(authUser.getUsername(), new Entity<>(nextId, authUser));
        return nextId++;
    }

}
