package com.icecoldcode.api.greeting;

import java.util.Objects;

public record GreetingDto(long id, String author, String company, String message) {

    public GreetingDto {
        Objects.requireNonNull(author);
        Objects.requireNonNull(company);
        Objects.requireNonNull(message);
    }

}
