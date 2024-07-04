package com.icecoldcode.core.authentication;

import com.icecoldcode.core.authentication.internal.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth/users")
@RestController
class AuthUsersController {
    private final AuthenticationService authenticationService;

    public AuthUsersController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateAuthUserDto createAuthUserDto) {
        return new ResponseEntity<>(
                authenticationService.createUser(createAuthUserDto),
                HttpStatus.CREATED
        );
    }

}
