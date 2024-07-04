package com.icecoldcode.core.authentication;

import com.icecoldcode.core.authentication.internal.AuthenticationService;
import com.icecoldcode.core.authentication.internal.JwtService;
import com.icecoldcode.core.authentication.internal.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/auth")
@RestController
class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService authenticationService,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public AuthUserDto authenticate(@RequestBody LoginDto loginDto,
                                    HttpServletResponse response) {
        var principal = authenticationService.authenticate(loginDto);
        response.addCookie(jwtService.generateJwtCookie(principal));

        return userRepository.getById(principal.userId())
                .map(authUserEntity ->
                        new AuthUserDto(
                                authUserEntity.value().getUsername(),
                                authUserEntity.value().getCompanyId(),
                                authUserEntity.value().isAdmin()
                        )
                ).orElseThrow();
    }

    @GetMapping
    public Optional<AuthUserDto> me(HttpServletRequest request) {
        return jwtService.principalFromCookie(request.getCookies())
                .flatMap(principal -> userRepository.getById(principal.userId()))
                .map(authUserEntity ->
                        new AuthUserDto(
                                authUserEntity.value().getUsername(),
                                authUserEntity.value().getCompanyId(),
                                authUserEntity.value().isAdmin()
                        )
                );
    }

    @DeleteMapping
    public void logout(HttpServletResponse response) {
        response.addCookie(jwtService.clearJwtCookie());
    }
}
