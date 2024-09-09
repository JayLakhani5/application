package com.Identity.Identity.jwt;


import com.Identity.Identity.request.TokenRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtUtil {

    private static final String SECRET_KEY = "your-very-secure-secret-key";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final JWTVerifier JWT_VERIFIER = JWT.require(ALGORITHM).build();

    public String generateToken(TokenRequest request) {

        if (request.getUserId() == null || request.getRoleId() == null) {
            throw new IllegalArgumentException("Invalid TokenRequest: userId, roleId, and sessionId must be provided.");
        }
        if (request.getRoleId().isEmpty()) {
            throw new IllegalArgumentException("Invalid TokenRequest: roleId cannot be empty.");
        }

        return JWT.create()
                .withClaim("userId", request.getUserId())
                .withClaim("roleId", request.getRoleId())
                .withClaim("sessionId", request.getSessionId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 3)) // 3hr
                .sign(ALGORITHM);
    }


    public DecodedJWT decodeToken(String token) {
        return JWT_VERIFIER.verify(token);
    }

    public Integer extractUserId(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT.getClaim("userId").asInt();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token or token claim missing");
        }
    }

    public List<Integer> extractRoleId(String token) {
        return decodeToken(token).getClaim("roleId").asList(Integer.class);
    }

    public String extractSessionId(String token) {
        DecodedJWT decodedJWT = JWT_VERIFIER.verify(token);
        return decodedJWT.getClaim("sessionId").asString();
    }

    public boolean isTokenExpired(String token) {
        return decodeToken(token).getExpiresAt().before(new Date());
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
