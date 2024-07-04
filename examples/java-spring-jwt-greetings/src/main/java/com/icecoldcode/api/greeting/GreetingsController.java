package com.icecoldcode.api.greeting;

import com.icecoldcode.core.SecurityContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/greetings")
class GreetingsController {

    private final SecurityContext securityContext;
    private final GreetingAuthorizationService greetingAuthorizationService;
    private final GreetingsRepository greetingsRepository;
    private final GreetingDtoAssembler greetingDtoAssembler;

    public GreetingsController(SecurityContext securityContext,
                               GreetingAuthorizationService greetingAuthorizationService,
                               GreetingsRepository greetingsRepository,
                               GreetingDtoAssembler greetingDtoAssembler) {
        this.securityContext = securityContext;
        this.greetingAuthorizationService = greetingAuthorizationService;
        this.greetingsRepository = greetingsRepository;
        this.greetingDtoAssembler = greetingDtoAssembler;
    }

    @GetMapping
    Collection<GreetingDto> list() {
        return greetingsRepository.list().stream()
                .map(greetingDtoAssembler::toDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        var principal = securityContext.requirePrincipal();

        return greetingsRepository.get(id)
                .filter(greetingAuthorizationService.canDelete(principal))
                .map(greetingEntity -> {
                    greetingsRepository.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public long create(@RequestBody CreateGreetingDto greetingDto) {
        var principal = securityContext.requirePrincipal();
        return greetingsRepository.create(new Greeting(
                principal.userId(),
                greetingDto.message()
        ));
    }

}
