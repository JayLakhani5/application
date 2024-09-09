package com.Identity.Identity.rest;

import com.Identity.Identity.jwt.JwtUtil;
import com.Identity.Identity.request.TokenRequest;
import com.Identity.Identity.response.TokenResponse;
import com.Identity.Identity.response.TokenValidationResponse;
import com.Identity.Identity.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IdentityController {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;


    @PostMapping("generate-token")
    public TokenResponse createToken(@RequestBody TokenRequest request) {

        String token = jwtUtil.generateToken(request);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken(token);
        return tokenResponse;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenResponse token) {
        TokenValidationResponse response = new TokenValidationResponse();
        try {

            if (jwtUtil.validateToken(token.getToken())) {
                UUID sessionId = jwtUtil.extractSessionId(token.getToken());
                System.out.println("Extracted Session ID: " + sessionId);

                if (!sessionService.isSessionValid(sessionId)) {
                    response.setValid(true);
                    response.setUserId(jwtUtil.extractUserId(token.getToken()));
                    response.setRoleId(jwtUtil.extractRoleId(token.getToken()));
                    response.setSessionId(sessionId);
                    return ResponseEntity.ok(response);
                } else {
                    System.out.println("Session is invalid for ID: " + sessionId);
                    response.setValid(false);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {

                System.out.println("Token is invalid or expired.");
                response.setValid(false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setValid(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/extractUserId")
    public ResponseEntity<?> extractUserId(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            Integer userId = jwtUtil.extractUserId(token);
            return ResponseEntity.ok(userId);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token format");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while extracting user ID");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {

            TokenValidationResponse response = new TokenValidationResponse();
            String token = extractTokenFromHeader(authorizationHeader);
            UUID sessionId = jwtUtil.extractSessionId(token);

            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired.");
            }
            // Invalidate the session by sessionId
            sessionService.invalidateSession(sessionId);
            response.setValid(false);
            return ResponseEntity.ok("Logout successful. Session invalidated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing logout.");
        }
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format.");
    }
}
