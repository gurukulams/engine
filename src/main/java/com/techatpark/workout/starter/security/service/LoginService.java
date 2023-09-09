package com.techatpark.workout.starter.security.service;

import com.techatpark.workout.model.AuthProvider;
import com.techatpark.workout.service.LearnerService;
import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import com.techatpark.workout.starter.security.util.TokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

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
     * @param authenticationRequest authenticationRequest
     */
    private void signUp(final AuthenticationRequest
                                authenticationRequest) throws SQLException {
        // Then Sign Up
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(authenticationRequest.getUserName());
        signupRequest.setPassword(authenticationRequest.getPassword());
        signupRequest.setAuthProvider(AuthProvider.local);
        signupRequest.setImageUrl("/images/"
                + authenticationRequest.getUserName().split("@")[0]
                + ".png");
        learnerService.signUp(signupRequest,
                passwordEncoder::encode);
    }

    /**
     * Login authentication response.
     *
     * @param authenticationRequest the authentication request
     * @return the authentication response
     */
    public AuthenticationResponse login(final AuthenticationRequest
                                    authenticationRequest) throws SQLException {
        try {
            return tokenProvider.getAuthenticationResponse(
                    this.authenticationManager
                    .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.getUserName(),
                                authenticationRequest.getPassword())));
        } catch (final BadCredentialsException credentialsException) {
            // If New User
            if (learnerService.readByEmail(
                    authenticationRequest.getUserName()).isEmpty()) {
                // Then Sign Up
                signUp(authenticationRequest);
                return login(authenticationRequest);
            }
            throw new BadCredentialsException("Invalid Login Credentials");
        }
    }
}
