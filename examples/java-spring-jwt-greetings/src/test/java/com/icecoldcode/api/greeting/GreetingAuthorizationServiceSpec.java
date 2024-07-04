package com.icecoldcode.api.greeting;

import com.icecoldcode.core.Clock;
import com.icecoldcode.core.Entity;
import com.icecoldcode.core.authentication.internal.AuthUser;
import com.icecoldcode.core.authentication.internal.Principal;
import com.icecoldcode.core.authentication.internal.PrincipalFactory;
import com.icecoldcode.core.authentication.internal.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class GreetingAuthorizationServiceSpec {

    private final PrincipalFactory principalFactory = new PrincipalFactory(new Clock());
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final GreetingAuthorizationService service = new GreetingAuthorizationService(
            userRepository
    );

    @Test
    void canDeleteIfAdminAndSameCompany() {
        var principal = createPrincipal(1, "companyA", Set.of("ROLE_ADMIN"));
        mockUserReturned(2, "companyA", Set.of());

        assertTrue(
                service.canDelete(principal).test(createGreeting(2))
        );
    }

    @Test
    void canDeleteIfAuthor() {
        var principal = createPrincipal(1, "companyA", Set.of());
        mockUserReturned(1, "companyA", Set.of());

        assertTrue(
                service.canDelete(principal).test(createGreeting(1))
        );
    }

    @Test
    void canNotDeleteIfAdminButDifferentCompany() {
        var principal = createPrincipal(1, "companyA", Set.of("ROLE_ADMIN"));
        mockUserReturned(2, "companyB", Set.of());

        assertFalse(
                service.canDelete(principal).test(createGreeting(2))
        );
    }

    @Test
    void canNotDeleteIfUserAndDifferentCompany() {
        var principal = createPrincipal(1, "companyA", Set.of());
        mockUserReturned(2, "companyB", Set.of());

        assertFalse(
                service.canDelete(principal).test(createGreeting(2))
        );
    }

    @Test
    void canNotDeleteIfUserAndSameCompanyButNotAuthor() {
        var principal = createPrincipal(1, "companyA", Set.of());
        mockUserReturned(2, "companyA", Set.of());

        assertFalse(
                service.canDelete(principal).test(createGreeting(2))
        );
    }

    private void mockUserReturned(long id, String companyId, Set<String> roles) {
        when(userRepository.getById(id)).thenReturn(Optional.of(new Entity<>(id,
                new AuthUser("", "", companyId, roles))));
    }

    private Principal createPrincipal(long id, String companyId, Set<String> roles) {
        return principalFactory.fromAuthUser(new Entity<>(id, new AuthUser(
                "", "", companyId, roles
        )));
    }

    private static Entity<Greeting> createGreeting(int authorId) {
        return new Entity<>(1, new Greeting(authorId, "hej"));
    }

}