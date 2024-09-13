package com.Identity.Identity.service;

import com.Identity.Identity.jwt.JwtUtil;
import com.Identity.Identity.request.TokenRequest;
import com.Identity.Identity.response.TokenResponse;
import com.Identity.Identity.response.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;

    public TokenResponse generateToken(TokenRequest request) {
        String token = jwtUtil.generateToken(request);
        long expiresIn = jwtUtil.getTokenExpirationTimeInMinutes(token);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken(token);
        tokenResponse.setExpireTime(expiresIn);
        return tokenResponse;
    }

    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();
        try {
            if (jwtUtil.isTokenExpired(token)) {
                response.setValid(false);
                response.setExpired(true);
            } else if (jwtUtil.validateToken(token)) {
                Map<String, Object> claims = jwtUtil.extractClaims(token);
                response.setValid(true);
                response.setUserId((Integer) claims.get("userId"));
                response.setRoleId((List<Integer>) claims.get("roleId"));
                response.setSessionId((String) claims.get("sessionId"));
                long expiresIn = jwtUtil.getTokenExpirationTimeInMinutes(token);
                response.setExpireTime(expiresIn);
                response.setExpired(false);
            } else {
                response.setValid(false);
                response.setExpired(false);
            }
        } catch (Exception e) {
            response.setValid(false);
            response.setExpired(true);
        }
        return response;
    }
}
