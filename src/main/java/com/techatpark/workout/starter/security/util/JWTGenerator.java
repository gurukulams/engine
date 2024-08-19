package com.techatpark.workout.starter.security.util;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public final class JWTGenerator {

    private JWTGenerator() {
    }

    /**
     * Gets Signin Key.
     * @param tokenSecret
     * @return SecretKey
     */
    public static SecretKey getSignInKey(final String tokenSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
