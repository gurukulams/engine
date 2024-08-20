package com.techatpark.workout.starter.security.service;

import com.gurukulams.core.payload.RegistrationRequest;
import com.gurukulams.core.service.LearnerProfileService;
import com.techatpark.workout.starter.security.config.AppProperties;
import com.techatpark.workout.starter.security.config.UserPrincipal;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RefreshToken;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static com.techatpark.workout.starter.security.util.JWTGenerator.getUserNameFromToken;
import static com.techatpark.workout.starter.security.util.JWTGenerator.getJWTCompact;
import static com.techatpark.workout.starter.security.util.JWTGenerator.isExpired;
/**
 * The type Token provider.
 */
public class AuthenticationService {

    /**
     * value.
     */
    private static final int VALUE = 7;

    /**
     * Cache Manager.
     */
    private final CacheManager cacheManager;

    /**
     * Cache to hold auth tokens.
     */
    private final Cache authCache;
    /***
     * hhh.
     */
    private final AppProperties appProperties;

    /**
     * UserDetailsService.
     */
    private UserDetailsService userDetailsService;

    /**
     * LearnerProfileService.
     */
    private LearnerProfileService learnerProfileService;

    /**
     * gg.
     *
     * @param appPropertie           the app propertie
     * @param acacheManager
     * @param auserDetailsService
     * @param alearnerProfileService
     */
    public AuthenticationService(final AppProperties appPropertie,
                     final CacheManager acacheManager,
                     final UserDetailsService auserDetailsService,
                     final LearnerProfileService alearnerProfileService) {
        this.appProperties = appPropertie;
        this.cacheManager = acacheManager;
        this.userDetailsService = auserDetailsService;
        this.authCache = cacheManager.getCache("Auth");
        this.learnerProfileService = alearnerProfileService;
    }

    /**
     * Gets Authentication.
     * @param requestURI
     * @param jwt
     * @return authentication
     */
    public UsernamePasswordAuthenticationToken getAuthentication(
                            final String requestURI,
                            final String jwt) {
        final String userName =
                getUserNameFromToken(requestURI, jwt,
                        appProperties.getAuth().getTokenSecret(),
                        authCache);
        final UserDetails userDetails =
                userDetailsService.loadUserByUsername(userName);
        return new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());
    }


    /**
     * generate AuthenticationResponse.
     *
     * @param authentication the authentication
     * @return token string
     */
    public AuthenticationResponse getAuthenticationResponse(
            final Authentication authentication) {
        return getAuthenticationResponse(authentication.getName());
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
                appProperties.getAuth().getTokenExpirationMsec(),
                appProperties.getAuth().getTokenSecret()));
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


    private String getBearer(final String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader
                .startsWith("Bearer ")) {
            return authHeader.substring(VALUE);
        }
        throw new BadCredentialsException("Invalid Token");
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
    @Validated
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
                    appProperties.getAuth().getTokenSecret())) {
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
                    appProperties.getAuth().getTokenExpirationMsec(),
                    this.generateRefreshToken(authToken),
                    null,
                    userPrincipal.getProfilePicture(),
                    this.appProperties.getFeature()
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
