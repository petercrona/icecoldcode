package com.icecoldcode.core.authentication.internal;

import com.icecoldcode.core.authentication.CreateAuthUserDto;
import com.icecoldcode.core.authentication.LoginDto;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final PrincipalFactory principalFactory;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationConfiguration authenticationConfiguration,
            PasswordEncoder passwordEncoder,
            PrincipalFactory principalFactory
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.principalFactory = principalFactory;
    }

    public long createUser(CreateAuthUserDto createAuthUserDto) {
        var roles = createAuthUserDto.roles();
        var rolesWithUser = new HashSet<>(roles);
        rolesWithUser.add("ROLE_USER");

        return userRepository.save(new AuthUser(
                createAuthUserDto.username(),
                passwordEncoder.encode(createAuthUserDto.password()),
                createAuthUserDto.companyId(),
                rolesWithUser
        ));
    }

    public Principal authenticate(LoginDto input) {
        try {
            authenticationConfiguration.getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.username(),
                            input.password()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("failed to authenticate");
        }

        return userRepository.getByEmail(input.username())
                .map(principalFactory::fromAuthUser)
                .orElseThrow();

    }

}
