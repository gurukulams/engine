package com.techatpark.workout.starter.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

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
            throw ex;
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
     * @param requestURI
     * @param jwtToken
     * @param tokenSecret
     * @return token. user name from token
     */
    public static String getUserNameFromToken(final String requestURI,
                   final String jwtToken,
                   final String tokenSecret) {




        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey(tokenSecret))
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            return claims.getSubject();
        } catch (final MalformedJwtException | UnsupportedJwtException
                       | IllegalArgumentException ex) {
            throw ex;
        } catch (final ExpiredJwtException ex) {
            if (requestURI.equals("/api/auth/logout")
                    || requestURI.equals("/api/auth/refresh")) {
                return ex.getClaims().getSubject();
            } else {
                throw ex;
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
