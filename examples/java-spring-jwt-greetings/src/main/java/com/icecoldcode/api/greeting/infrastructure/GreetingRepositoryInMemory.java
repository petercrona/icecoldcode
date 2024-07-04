package com.icecoldcode.api.greeting.infrastructure;

import com.icecoldcode.api.greeting.Greeting;
import com.icecoldcode.api.greeting.GreetingsRepository;
import com.icecoldcode.core.Entity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class GreetingRepositoryInMemory implements GreetingsRepository {

    public static long nextId = 1;

    private final Map<Long, Entity<Greeting>> store = new HashMap<>();

    @Override
    public Collection<Entity<Greeting>> list() {
        return store.values();
    }

    @Override
    public Optional<Entity<Greeting>> get(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void delete(long id) {
        store.remove(id);
    }

    @Override
    public long create(Greeting greetingDto) {
        store.put(nextId, new Entity<>(nextId, greetingDto));
        return nextId++;
    }
}
