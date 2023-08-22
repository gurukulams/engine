package com.techatpark.workout.starter.security.controller;

import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RefreshToken;
import com.techatpark.workout.starter.security.payload.RegistrationRequest;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.service.AuthenticationService;
import com.techatpark.workout.starter.security.service.LearnerService;
import com.techatpark.workout.starter.security.service.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.security.Principal;

/**
 * The type Authentication api controller.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication",
        description = "Resource to manage authentication")
class AuthenticationAPIController {

    /**
     * instance of AuthenticationService.
     */
    private final AuthenticationService authenticationService;


    AuthenticationAPIController(final AuthenticationService
                                        paramAuthenticationService) {
        this.authenticationService = paramAuthenticationService;
    }


    @Operation(summary = "Signup the User")
    @PostMapping("/signup")
    public final ResponseEntity<Void> signUp(
            final @RequestBody SignupRequest signUpRequest) {
        authenticationService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * @param registrationRequest
     * @param authHeader
     * @param principal
     * @return loginRequest
     */
    @Operation(summary = "Register the User")
    @PostMapping("/register")
    public final ResponseEntity<AuthenticationResponse> register(
            final Principal principal,
            @RequestHeader(name = "Authorization") final String authHeader,
            final @RequestBody RegistrationRequest registrationRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                authenticationService.register(authHeader, principal,
                        registrationRequest));
    }

    /**
     * performs the login function.
     *
     * @param authenticationRequest the authentication request
     * @return authentication response
     */
    @Operation(summary = "Login with credentials")
    @PostMapping("/login")
    public final ResponseEntity<AuthenticationResponse> login(
            final @RequestBody
            AuthenticationRequest
                    authenticationRequest) {

        return ResponseEntity.ok().body(
                authenticationService.login(authenticationRequest));
    }

    /**
     * performs the login function.
     *
     * @param authHeader
     * @param refreshToken the authentication request
     * @param principal
     * @return authentication response
     */
    @Operation(summary = "Refresh the credentials")
    @PostMapping("/refresh")
    public final ResponseEntity<AuthenticationResponse> refresh(
            final Principal principal,
            @RequestHeader(name = "Authorization") final String authHeader,
            final @RequestBody
            RefreshToken
                    refreshToken) {
        return ResponseEntity.ok().body(authenticationService.refresh(
                authHeader, refreshToken, principal));
    }

    /**
     * logout an user.
     *
     * @param authHeader
     * @return void response entity
     */
    @Operation(summary = "logout current user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public final ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization") final String authHeader) {
        authenticationService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    /**
     * get the user details from the principal.
     *
     * @param authHeader the auth Header
     * @return AuthenticationResponse response entity
     */
    @Operation(summary = "Get logged in user profile",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "practice"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "practice not found")})
    @GetMapping("/me")
    public final ResponseEntity<AuthenticationResponse> me(
            @RequestHeader(name = "Authorization") final String authHeader) {
        return ResponseEntity.ok().body(authenticationService
                .getWelcomeResponse(authHeader));
    }
}
