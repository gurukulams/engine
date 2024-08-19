package com.techatpark.workout.starter.security.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public final class JWTGenerator {

    private JWTGenerator() {
    }

    /**
     * isExpired.
     * @param token the auth token
     * @param tokenSecret
     * @return dd. boolean
     */
    public static boolean isExpired(final String token,
                              final String tokenSecret) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey(
                            tokenSecret))
                    .build()
                    .parseSignedClaims(token);
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid Token", ex);
        } catch (final ExpiredJwtException ex) {
            return true;
        }
        return false;
    }

    /**
     * Get Compact JWT.
     * @param userName
     * @param expiration
     * @param tokenSecret
     * @return jwt
     */
    public static String getJWTCompact(final String userName,
                                 final long expiration,
                                 final String tokenSecret) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userName)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now
                        + expiration))
                .signWith(getSignInKey(tokenSecret),
                        SignatureAlgorithm.HS256).compact();
    }

    /**
     * gg.
     *
     * @param token the token
     * @param requestURI
     * @param tokenSecret
     * @param authCache
     * @param objectMapper
     * @return token. user name from token
     */
    public static String getUserNameFromToken(final String requestURI,
                   final String token,
                   final String tokenSecret,
                   final Cache authCache,
                   final ObjectMapper objectMapper) {


        Cache.ValueWrapper valueWrapper = authCache.get(token);

        if (valueWrapper == null) {
            throw new BadCredentialsException("Invalid Token");
        }

        String jwtToken = valueWrapper.get().toString();



        try {
            final Claims claims = Jwts.parser()
//                    parserBuilder()
                    .verifyWith(getSignInKey(tokenSecret))
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getBody();
            return claims.getSubject();
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid Token", ex);
        } catch (final ExpiredJwtException ex) {
            if (requestURI.equals("/api/auth/logout")
                    || requestURI.equals("/api/auth/refresh")) {
                return getUserNameFromExpiredToken(jwtToken, objectMapper);
            } else {
                throw new BadCredentialsException("Expired Token", ex);
            }
        }

    }
     /**
     * Gets Username from Expired Token.
     * @param token
     * @param objectMapper
     * @return userName
     */
    public static String getUserNameFromExpiredToken(final String token,
                  final ObjectMapper objectMapper)  {

        Base64.Decoder decoder = Base64.getUrlDecoder();
        // Splitting header, payload and signature
        String[] parts = token.split("\\.");
        // String headers = new String(decoder.decode(parts[0]));
        String payload =
                new String(decoder.decode(parts[1])); // Payload
        String userName;
        try {
            userName = objectMapper.readTree(payload).get("sub").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userName;
    }

    /**
     * Gets Signin Key.
     * @param tokenSecret
     * @return SecretKey
     */
    private static SecretKey getSignInKey(final String tokenSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
