package com.usermanagement.usermanagement.web;

import com.usermanagement.usermanagement.dto.Login;
import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.dto.UserResponseDTO;
import com.usermanagement.usermanagement.exception.UserManagementException;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.service.UserSingUpService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class UserSingUpRestController {

    private final UserSingUpService userSingUpService;
    private final JwtClient jwtClient;

    @PostMapping("/singup")
    public UserResponseDTO addUser(@RequestBody UserRequest userRequest) {
        return userSingUpService.addUser(userRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login4(@RequestBody Login login) {
        return userSingUpService.loginUser(login.getEmailOrContactNumber(), login.getPassword());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout1(@RequestHeader("Authorization") String authHeader) {
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse tokenResponse;
        try {
            tokenResponse = jwtClient.validateToken(getToken);
        } catch (FeignException e) {
            if (e.status() == 401) {
                throw new UserManagementException("Token is invalid or malformed");
            } else {
                throw new UserManagementException("Error validating token", e);
            }
        }

        if (tokenResponse.isExpired()) {
            throw new UserManagementException("Token is expired");
        }
        UUID sessionId;
        try {
            sessionId = UUID.fromString(tokenResponse.getSessionId());
        } catch (IllegalArgumentException e) {
            throw new UserManagementException("Invalid session ID");
        }
        try {
            userSingUpService.logout(sessionId);
        } catch (RuntimeException e) {
            throw new UserManagementException(e.getMessage());
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }
}
