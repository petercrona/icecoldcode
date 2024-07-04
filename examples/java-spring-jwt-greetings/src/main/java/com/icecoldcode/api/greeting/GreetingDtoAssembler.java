package com.icecoldcode.api.greeting;

import com.icecoldcode.core.Entity;
import com.icecoldcode.core.authentication.internal.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GreetingDtoAssembler {

    private final UserRepository userRepository;

    public GreetingDtoAssembler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<GreetingDto> toDto(Entity<Greeting> greetingEntity) {
        return userRepository.getById(greetingEntity.value().authorId()).map(authUserEntity ->
                new GreetingDto(
                        greetingEntity.id(),
                        authUserEntity.value().getUsername(),
                        authUserEntity.value().getCompanyId(),
                        greetingEntity.value().message()
                )
        );
    }

}
