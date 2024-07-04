package com.icecoldcode.api.greeting;

import java.util.Objects;

public record Greeting(long authorId, String message) {

    public Greeting {
        Objects.requireNonNull(message);
    }

}
