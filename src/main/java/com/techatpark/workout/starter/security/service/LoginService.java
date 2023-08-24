package com.techatpark.workout.starter.security.service;

import com.techatpark.workout.service.LearnerService;
import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.util.TokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The type Login service.
 */
@Service
public class LoginService {

    /**
     * LearnerService instance.
     */
    private final LearnerService learnerService;

    /**
     * PasswordEncoder instance.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * AuthenticationManager instance.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * TokenProvider instance.
     */
    private final TokenProvider tokenProvider;

    /**
     * Instantiates a new Login service.
     *
     * @param aLearnerService        the a learner service
     * @param aPasswordEncoder       the a password encoder
     * @param aAuthenticationManager the a authentication manager
     * @param aTokenProvider         the a token provider
     */
    public LoginService(final LearnerService aLearnerService,
                        final PasswordEncoder aPasswordEncoder,
                        final AuthenticationManager aAuthenticationManager,
                        final TokenProvider aTokenProvider) {
        this.learnerService = aLearnerService;
        this.passwordEncoder = aPasswordEncoder;
        this.authenticationManager = aAuthenticationManager;
        this.tokenProvider = aTokenProvider;
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
