package com.techatpark.workout.starter.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.model.LearnerProfile;
import com.gurukulams.core.service.LearnerProfileService;
import com.techatpark.workout.starter.security.config.AppProperties;
import com.techatpark.workout.starter.security.config.UserPrincipal;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RefreshToken;
import com.techatpark.workout.starter.security.payload.RegistrationRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.security.Key;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

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
     * Object Mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Validator.
     */
    @Autowired
    private Validator validator;

    /**
     * Cache to hold auth tokens.
     */
    private final Cache authCache;
    /***
     * hhh.
     */
    private AppProperties appProperties;

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
     * @param aobjectMapper
     * @param auserDetailsService
     * @param alearnerProfileService
     */
    public AuthenticationService(final AppProperties appPropertie,
                     final CacheManager acacheManager,
                     final ObjectMapper aobjectMapper,
                     final UserDetailsService auserDetailsService,
                     final LearnerProfileService alearnerProfileService) {
        this.objectMapper = aobjectMapper;
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
                getUserNameFromToken(requestURI, jwt);

        final UserDetails userDetails =
                userDetailsService.loadUserByUsername(userName);
        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(),
                        userDetails.getAuthorities());

        return authentication;
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
                appProperties.getAuth().getTokenExpirationMsec()));
        return token;

    }

    private String getJWTCompact(final String userName,
                                 final long expiration) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userName)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now
                        + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }


    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getAuth()
                .getTokenSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * gg.
     *
     * @param token the token
     * @param requestURI
     * @return token. user name from token
     */
    public String getUserNameFromToken(final String requestURI,
                                       final String token) {


        Cache.ValueWrapper valueWrapper = authCache.get(token);

        if (valueWrapper == null) {
            throw new BadCredentialsException("Invalid Token");
        }

        String jwtToken = valueWrapper.get().toString();



        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return claims.getSubject();
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid Token", ex);
        } catch (final ExpiredJwtException ex) {
            if (requestURI.equals("/api/auth/logout")
                   || requestURI.equals("/api/auth/refresh")) {
                return getUserNameFromExpiredToken(jwtToken);
            } else {
                throw new BadCredentialsException("Expired Token", ex);
            }
        }

    }

    /**
     * Gets Username from Expired Token.
     * @param token
     * @return userName
     */
    public String getUserNameFromExpiredToken(final String token)  {

        Base64.Decoder decoder = Base64.getUrlDecoder();
        // Splitting header, payload and signature
        String[] parts = token.split("\\.");
        // String headers = new String(decoder.decode(parts[0]));
        String payload =
                new String(decoder.decode(parts[1])); // Payload
        String userName;
        try {
            userName = this.objectMapper.readTree(payload).get("sub").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userName;
    }

    /**
     * ddd.
     * @param token the auth token
     * @return dd. boolean
     */
    private boolean isExpired(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey()).build()
                    .parseClaimsJws(token);
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid Token", ex);
        } catch (final ExpiredJwtException ex) {
            return true;
        }
        return false;
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
     * @param userName
     * @param registrationRequest
     * @return authenticationResponse
     */
    @Validated
    public AuthenticationResponse register(final String authHeader,
                              final Principal userName,
                              final RegistrationRequest registrationRequest)
            throws SQLException {

        Set<ConstraintViolation<RegistrationRequest>> violations =
                validator.validate(registrationRequest);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        String registrationToken = getBearer(authHeader);
        String[] parts = userName.getName().split("@");
        String userHandle = parts[0];
        LearnerProfile learnerProfile = new LearnerProfile();
        learnerProfile.setUserHandle(userHandle);
        learnerProfile.setName(registrationRequest.getName());
        learnerProfile.setDob(registrationRequest.getDob());
        learnerProfileService.create(learnerProfile);

        authCache.evict(registrationToken);
        return getAuthenticationResponse(userName.getName());
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

            if (!isExpired(authTokenCache.get().toString())) {
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
                    authToken,
                    appProperties.getAuth().getTokenExpirationMsec(),
                    this.generateRefreshToken(authToken),
                    null,
                    userPrincipal.getProfilePicture());
        }

        return new AuthenticationResponse(userName,
                null,
                null,
                null,
                generateToken(userName),
                userPrincipal.getProfilePicture());
    }
}
