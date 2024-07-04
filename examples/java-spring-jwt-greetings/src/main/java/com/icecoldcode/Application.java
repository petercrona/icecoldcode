package com.icecoldcode;

import com.icecoldcode.api.greeting.Greeting;
import com.icecoldcode.api.greeting.GreetingsRepository;
import com.icecoldcode.core.authentication.CreateAuthUserDto;
import com.icecoldcode.core.authentication.internal.AuthenticationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;

@SpringBootApplication
public class Application {

    public Application(AuthenticationService authenticationService,
                       GreetingsRepository greetingsRepository) {
        // test data
        authenticationService.createUser(new CreateAuthUserDto(
                "test",
                "test",
                "test",
                Set.of("ROLE_ADMIN")
        ));

        greetingsRepository.create(new Greeting(1,
                "Hello, I hope you are well! I wish you a very pleasant evening."
        ));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

