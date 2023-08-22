package com.techatpark.workout.starter.security.service;

import com.techatpark.workout.starter.security.payload.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * The type Authentication service.
 */
@Service
public class AuthenticationService {

    /**
     * instance of PasswordEncoder.
     */
    private final PasswordEncoder passwordEncoder;

    private final LearnerService learnerService;

    private final TokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;


    /**
     * Instantiates a new Authentication service.
     *
     * @param paramPasswordEncoder       the param password encoder
     * @param paramLearnerService        the param learner service
     * @param paramTokenProvider         the param token provider
     * @param paramAuthenticationManager the param authentication manager
     */
    public AuthenticationService(PasswordEncoder paramPasswordEncoder,
                                 LearnerService paramLearnerService,
                                 TokenProvider paramTokenProvider,
                                 AuthenticationManager
                                         paramAuthenticationManager) {
        this.passwordEncoder = paramPasswordEncoder;
        this.learnerService = paramLearnerService;
        this.tokenProvider = paramTokenProvider;
        this.authenticationManager = paramAuthenticationManager;
    }

    /**
     * Sign up.
     *
     * @param signUpRequest the sign up request
     */
    public void signUp(SignupRequest signUpRequest) {
        learnerService.signUp(signUpRequest,
                passwordEncoder::encode);
    }

    /**
     * Logout.
     *
     * @param authHeader the auth header
     */
    public void logout(String authHeader) {
        tokenProvider.logout(authHeader);
    }

    /**
     * Gets welcome response.
     *
     * @param authHeader the auth header
     * @return the welcome response
     */
    public AuthenticationResponse getWelcomeResponse(String authHeader) {
        return tokenProvider.getWelcomeResponse(authHeader);
    }

    /**
     * Refresh authentication response.
     *
     * @param authHeader   the auth header
     * @param refreshToken the refresh token
     * @param principal    the principal
     * @return the authentication response
     */
    public AuthenticationResponse refresh(String authHeader,
                                          RefreshToken refreshToken,
                                          Principal principal) {
        return tokenProvider.refresh(authHeader, principal.getName(),
                refreshToken);
    }

    /**
     * Register authentication response.
     *
     * @param authHeader          the auth header
     * @param principal           the principal
     * @param registrationRequest the registration request
     * @return the authentication response
     */
    public AuthenticationResponse register(String authHeader,
                                           Principal principal,
                                           RegistrationRequest
                                                   registrationRequest) {
        return tokenProvider.register(authHeader,
                principal.getName(), registrationRequest);
    }

    /**
     * Login authentication response.
     *
     * @param authenticationRequest the authentication request
     * @return the authentication response
     */
    public AuthenticationResponse login(AuthenticationRequest
                                                authenticationRequest) {
        final Authentication authResult = this.authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.getUserName(),
                                authenticationRequest.getPassword()));
        if (authResult == null) {
            throw new BadCredentialsException("Invalid Login Credentials");
        }

        return tokenProvider.getAuthenticationResponse(authResult);
    }
}
