package com.techatpark.workout.starter.security.service;


import com.techatpark.workout.service.LearnerService;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RefreshToken;
import com.techatpark.workout.starter.security.payload.RegistrationRequest;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.util.TokenProvider;
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

    /**
     * instance of LearnerService.
     */
    private final LearnerService learnerService;

    /**
     * instance of TokenProvider.
     */
    private final TokenProvider tokenProvider;

    /**
     * instance of AuthenticationManager.
     */
    private final AuthenticationManager authenticationManager;


    /**
     * Instantiates a new Authentication service.
     *
     * @param paramPasswordEncoder       the param password encoder
     * @param paramLearnerService        the param learner service
     * @param paramTokenProvider         the param token provider
     * @param paramAuthenticationManager the param authentication manager
     */
    public AuthenticationService(final PasswordEncoder paramPasswordEncoder,
                                 final LearnerService paramLearnerService,
                                 final TokenProvider paramTokenProvider,
                                 final AuthenticationManager
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
    public void signUp(final SignupRequest signUpRequest) {
        learnerService.signUp(signUpRequest,
                passwordEncoder::encode);
    }

    /**
     * Logout.
     *
     * @param authHeader the auth header
     */
    public void logout(final String authHeader) {
        tokenProvider.logout(authHeader);
    }

    /**
     * Gets welcome response.
     *
     * @param authHeader the auth header
     * @return the welcome response
     */
    public AuthenticationResponse getWelcomeResponse(final String authHeader) {
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
    public AuthenticationResponse refresh(final String authHeader,
                                          final RefreshToken refreshToken,
                                          final Principal principal) {
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
    public AuthenticationResponse register(final String authHeader,
                                           final Principal principal,
                                           final RegistrationRequest
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
    public AuthenticationResponse login(final AuthenticationRequest
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
