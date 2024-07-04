package com.icecoldcode.api.greeting;

import com.icecoldcode.core.Entity;
import com.icecoldcode.core.SecurityContext;
import com.icecoldcode.core.authentication.internal.Principal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GreetingsControllerSpec {

    private final SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    private final GreetingsRepository greetingRepository = Mockito.mock(GreetingsRepository.class);
    private final GreetingAuthorizationService greetingAuthorizationService =
            Mockito.mock(GreetingAuthorizationService.class);

    private final GreetingsController greetingsController = new GreetingsController(
            securityContext,
            greetingAuthorizationService,
            greetingRepository,
            Mockito.mock(GreetingDtoAssembler.class)
    );

    @Test
    void deletesUserIfAuthorizationPassed() {
        when(greetingAuthorizationService.canDelete(any())).thenReturn(obj -> true);
        when(greetingRepository.get(1)).thenReturn(createGreeting());

        greetingsController.deleteUser(1);

        verify(greetingRepository, times(1)).delete(1);
    }

    @Test
    void doesNotDeleteUserIfAuthorizationFail() {
        when(greetingAuthorizationService.canDelete(any())).thenReturn(obj -> false);
        when(greetingRepository.get(1)).thenReturn(createGreeting());

        greetingsController.deleteUser(1);

        verify(greetingRepository, never()).delete(anyLong());
    }

    @Test
    void createUserWithPrincipalAsAuthor() {
        when(securityContext.requirePrincipal()).thenReturn(new Principal(123,
                "",
                Set.of(),
                Instant.ofEpochMilli(0),
                Instant.ofEpochMilli(0)));

        greetingsController.create(new CreateGreetingDto("hej"));

        verify(greetingRepository, times(1)).create(
                new Greeting(123, "hej")
        );
    }

    private static Optional<Entity<Greeting>> createGreeting() {
        return Optional.of(new Entity<>(1, new Greeting(1, "")));
    }
}