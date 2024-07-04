package com.icecoldcode.api.greeting;

import com.icecoldcode.core.Entity;

import java.util.Collection;
import java.util.Optional;

public interface GreetingsRepository {

    Collection<Entity<Greeting>> list();

    Optional<Entity<Greeting>> get(long id);

    void delete(long id);

    long create(Greeting greeting);

}
