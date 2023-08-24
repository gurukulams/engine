package com.techatpark.workout.starter.security.controller;

import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Authentication api controller.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Login",
        description = "Resource to manage Login and Signup")
class LoginController {

    /**
     * LoginService instance.
     */
    private final LoginService loginService;

    /**
     * Instance of LoginController.
     *
     * @param aLoginService
     */
    LoginController(final LoginService aLoginService) {
        this.loginService = aLoginService;
    }

    /**
     * Endpoint to signup.
     *
     * @param signUpRequest
     * @return void
     */
    @Operation(summary = "Signup the User")
    @PostMapping("/signup")
    final ResponseEntity<Void> signUp(
            final @RequestBody SignupRequest signUpRequest) {
        loginService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * performs the login function.
     *
     * @param authenticationRequest the authentication request
     * @return authentication response
     */
    @Operation(summary = "Login with credentials")
    @PostMapping("/login")
    final ResponseEntity<AuthenticationResponse> login(
            final @RequestBody
            AuthenticationRequest
                    authenticationRequest) {

        return ResponseEntity.ok().body(
                loginService.login(authenticationRequest));
    }
}
