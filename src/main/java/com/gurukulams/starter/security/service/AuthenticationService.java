package com.gurukulams.starter.security.service;

import com.gurukulams.core.payload.RegistrationRequest;
import com.gurukulams.core.service.LearnerProfileService;
import com.gurukulams.starter.security.config.UserPrincipal;
import com.gurukulams.starter.security.payload.AuthenticationResponse;
import com.gurukulams.starter.security.payload.RefreshToken;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.gurukulams.starter.security.util.JWTGenerator.getBearer;
import static com.gurukulams.starter.security.util.JWTGenerator.isExpired;
import static com.gurukulams.starter.security.util.JWTGenerator.getJWTCompact;
import static com.gurukulams.starter.security.util.JWTGenerator.getUserNameFromToken;

/**
 * The type Token provider.
 */
public class AuthenticationService {

    /**
     * Cache to hold token Secret.
     */
    private final String tokenSecret;

    /**
     * Cache to hold auth tokens.
     */
    private final Cache authCache;

    /**
     * Cache to hold auth tokens.
     */
    private final long tokenExpirationMsec;

    /**
     * Features of the User.
     */
    private final Map<String, List<String>> features;

    /**
     * UserDetailsService.
     */
    private final UserDetailsService userDetailsService;

    /**
     * LearnerProfileService.
     */
    private final LearnerProfileService learnerProfileService;

    /**
     * gg.
     * @param aTokenSecret
     * @param aTokenExpirationMsec
     * @param theFeatures
     * @param aAuthCache
     * @param auserDetailsService
     * @param alearnerProfileService
     */
    public AuthenticationService(final String aTokenSecret,
                     final long aTokenExpirationMsec,
                     final Map<String, List<String>> theFeatures,
                     final Cache aAuthCache,
                     final UserDetailsService auserDetailsService,
                     final LearnerProfileService alearnerProfileService) {
        this.tokenSecret = aTokenSecret;
        this.tokenExpirationMsec = aTokenExpirationMsec;
        this.features = theFeatures;
        this.userDetailsService = auserDetailsService;
        this.authCache = aAuthCache;
        this.learnerProfileService = alearnerProfileService;
    }

    /**
     * Gets Authentication.
     * @param requestURI
     * @param token
     * @return authentication
     */
    public UsernamePasswordAuthenticationToken getAuthentication(
                            final String requestURI,
                            final String token) {
        Cache.ValueWrapper valueWrapper = authCache.get(token);

        if (valueWrapper == null) {
            throw new IllegalArgumentException("Invalid Token");
        }

        String jwtToken = valueWrapper.get().toString();


        final String userName =
                getUserNameFromToken(requestURI, jwtToken,
                        tokenSecret);
        final UserDetails userDetails =
                userDetailsService.loadUserByUsername(userName);
        return new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());
    }


    /**
     * generate AuthenticationResponse.
     *
     * @param principal the principal
     * @return token string
     */
    public AuthenticationResponse getAuthenticationResponse(
            final Principal principal) {
        return getAuthenticationResponse(principal.getName());
    }
    /**
     * generate AuthenticationResponse.
     *
     * @param authHeader the auth Header
     * @return token string
     */
    public AuthenticationResponse getWelcomeResponse(
            final String authHeader) {
        Cache.ValueWrapper valueWrapper = authCache.get(authHeader);

        if (valueWrapper == null) {
            throw new BadCredentialsException("Invalid Token");
        }
        AuthenticationResponse response =
                getAuthenticationResponse(valueWrapper.get().toString());

        authCache.evict(authHeader);

        return response;
    }
    /**
     * Generates Welcome Token.
     * @param userName
     * @return welcomeToken
     */
    public String generateWelcomeToken(final String userName) {
        String welcomeToken = UUID.randomUUID().toString();
        this.authCache.put(welcomeToken, userName);
        return welcomeToken;
    }

    /**
     * generate token after login.
     *
     * @param userName the userName
     * @return token string
     */
    private String generateToken(final String userName) {
        String token = UUID.randomUUID().toString();
        this.authCache.put(token, getJWTCompact(userName,
                tokenExpirationMsec,
                tokenSecret));
        return token;

    }

    /**
     * Logs Out user.
     *
     * @param authHeader
     */
    public void logout(final String authHeader) {
        authCache.evict(getBearer(authHeader));
    }




    /**
     * Generates Refresh Token.
     * @param token
     * @return refreshToken
     */
    public String generateRefreshToken(final String token) {
        String refreshToken = UUID.randomUUID().toString();
        this.authCache.put(refreshToken, token);
        return refreshToken;
    }

    /**
     * refresh.
     * @param authHeader
     * @param principal
     * @param registrationRequest
     * @return authenticationResponse
     */
    public AuthenticationResponse register(final String authHeader,
                              final Principal principal,
                              final RegistrationRequest registrationRequest)
            throws SQLException {

        learnerProfileService.create(principal.getName(),
                registrationRequest);

        authCache.evict(getBearer(authHeader));
        return getAuthenticationResponse(principal.getName());
    }

    /**
     * refresh.
     * @param authHeader
     * @param userName
     * @param refreshToken
     * @return authenticationResponse
     */
    public AuthenticationResponse refresh(final String authHeader,
                                final Principal userName,
                                final RefreshToken refreshToken) {

        // Cleanup Existing Tokens.
        Cache.ValueWrapper refreshTokenCache = authCache
                .get(refreshToken.getToken());


        if (refreshTokenCache == null) {
            throw new BadCredentialsException("Refresh Token unavailable");
        } else {
            String authToken = refreshTokenCache.get().toString();

            Cache.ValueWrapper authTokenCache = authCache
                    .get(authToken);

            if (authTokenCache == null) {
                throw new BadCredentialsException("Invalid Token");
            }

            if (!isExpired(authTokenCache.get().toString(),
                    tokenSecret)) {
                throw new BadCredentialsException("Token is not Expired Yet");
            }

            if (!authToken.equals(getBearer(authHeader))) {
                throw new BadCredentialsException("Tokens are not matching");
            }

            authCache.evict(refreshToken.getToken());
            authCache.evict(authToken);

            return getAuthenticationResponse(userName.getName());
        }

    }

    private AuthenticationResponse getAuthenticationResponse(
            final String userName) {
        UserPrincipal userPrincipal =
                (UserPrincipal) userDetailsService
                        .loadUserByUsername(userName);
        String authToken = generateToken(userName);

        if (userPrincipal.isRegistered()) {
            return new AuthenticationResponse(userName,
                    userPrincipal.getDisplayName(),
                    authToken,
                    tokenExpirationMsec,
                    this.generateRefreshToken(authToken),
                    null,
                    userPrincipal.getProfilePicture(),
                    this.features
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().contains(userName))
                        .map(Map.Entry::getKey)
                        .toList());
        }

        return new AuthenticationResponse(userName,
                null,
                null,
                null,
                null,
                generateToken(userName),
                userPrincipal.getProfilePicture(),
                null);
    }
}
