package com.icecoldcode.api.greeting;

import java.util.Objects;

public record CreateGreetingDto(String message) {

    public CreateGreetingDto {
        Objects.requireNonNull(message);
    }

}
