package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.Login;
import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.dto.UserResponseDTO;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.service.UserDemo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class UserDemoRest {

    private final UserDemo userDemo;
    private final JwtClient jwtClient;

    @PostMapping("/singup")
    public UserResponseDTO addUser(@RequestBody UserRequest userRequest) {
        return userDemo.addUser(userRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login4(@RequestBody Login login) {
        return userDemo.loginUser(login.getEmailOrContactNumber(), login.getPassword());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout1(@RequestHeader("Authorization") String authHeader) {
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse tokenResponse = jwtClient.validateToken(getToken);
        if (tokenResponse.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token is expired");
        }
        UUID sessionId;
        try {
            sessionId = UUID.fromString(tokenResponse.getSessionId());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid session ID", HttpStatus.BAD_REQUEST);
        }
        try {
            userDemo.logout(sessionId);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }
}
