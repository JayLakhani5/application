package com.Identity.Identity.jwt;


import com.Identity.Identity.request.TokenRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        algorithm = Algorithm.HMAC256(jwtProperties.getSecretKey());
        jwtVerifier = JWT.require(algorithm).build();
    }


    public String generateToken(TokenRequest request) {
        if (request.getUserId() == null || request.getRoleId() == null) {
            throw new IllegalArgumentException("Invalid TokenRequest: userId, roleId, and sessionId must be provided.");
        }
        if (request.getRoleId().isEmpty()) {
            throw new IllegalArgumentException("Invalid TokenRequest: roleId cannot be empty.");
        }

        long expirationMillis = jwtProperties.getTokenExpirationMinutes() * 60 * 1000;

        return JWT.create()
                .withClaim("userId", request.getUserId())
                .withClaim("roleId", request.getRoleId())
                .withClaim("sessionId", request.getSessionId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMillis))
                .sign(algorithm);
    }


    public DecodedJWT decodeToken(String token) {
        try {
            return jwtVerifier.verify(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public Map<String, Object> extractClaims(String token) {
        try {
            Map<String, Claim> claims = decodeToken(token).getClaims();
            return Map.of(
                    "userId", claims.get("userId").asInt(),
                    "roleId", claims.get("roleId").asList(Integer.class),
                    "sessionId", claims.get("sessionId").asString()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token or token claim missing", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return decodeToken(token).getExpiresAt().before(new Date());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token validation failed", e);
        }
    }

    public long getTokenExpirationTimeInMinutes(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            long currentTimeMillis = System.currentTimeMillis();
            long millisecondsRemaining = expiresAt.getTime() - currentTimeMillis;
            return millisecondsRemaining / (1000 * 60);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }
}