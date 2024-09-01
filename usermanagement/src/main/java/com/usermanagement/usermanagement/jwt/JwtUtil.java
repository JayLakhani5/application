package com.usermanagement.usermanagement.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "your-very-secure-secret-key"; // Replace with a more secure key
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final JWTVerifier JWT_VERIFIER = JWT.require(ALGORITHM).build();

    public static String generateToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .sign(ALGORITHM);
    }

    public static DecodedJWT decodeToken(String token) {
        return JWT_VERIFIER.verify(token);
    }

    public static String extractEmail(String token) {
        return decodeToken(token).getSubject();
    }

    public static boolean isTokenExpired(String token) {
        return decodeToken(token).getExpiresAt().before(new Date());
    }

    public static boolean validateToken(String token, String email) {
        return (email.equals(extractEmail(token)) && !isTokenExpired(token));
    }
}

