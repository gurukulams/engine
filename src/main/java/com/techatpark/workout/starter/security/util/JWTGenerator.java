package com.techatpark.workout.starter.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
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
                .claims(new HashMap<>())
                .subject(userName)
                .issuedAt(new Date(now))
                .expiration(new Date(now
                        + expiration))
                .signWith(getSignInKey(tokenSecret),
                        Jwts.SIG.HS256).compact();
    }

    /**
     * gg.
     *
     * @param token the token
     * @param requestURI
     * @param tokenSecret
     * @param authCache
     * @return token. user name from token
     */
    public static String getUserNameFromToken(final String requestURI,
                   final String token,
                   final String tokenSecret,
                   final Cache authCache) {


        Cache.ValueWrapper valueWrapper = authCache.get(token);

        if (valueWrapper == null) {
            throw new BadCredentialsException("Invalid Token");
        }

        String jwtToken = valueWrapper.get().toString();



        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey(tokenSecret))
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            return claims.getSubject();
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid Token", ex);
        } catch (final ExpiredJwtException ex) {
            if (requestURI.equals("/api/auth/logout")
                    || requestURI.equals("/api/auth/refresh")) {
                return ex.getClaims().getSubject();
            } else {
                throw new BadCredentialsException("Expired Token", ex);
            }
        }

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
