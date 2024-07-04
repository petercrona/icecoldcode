package com.icecoldcode.api.greeting;

import com.icecoldcode.core.Entity;
import com.icecoldcode.core.authentication.internal.Principal;
import com.icecoldcode.core.authentication.internal.UserRepository;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class GreetingAuthorizationService {

    private final UserRepository userRepository;

    public GreetingAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Predicate<Entity<Greeting>> canDelete(Principal principal) {
        return greeting -> userRepository.getById(greeting.value().authorId())
                .map(authUserEntity -> {
                    if (!authUserEntity.value().getCompanyId().equals(principal.companyId())) {
                        return false;
                    }

                    return principal.isAdmin()
                            || greeting.value().authorId() == principal.userId();
                }).orElse(false);
    }

}
