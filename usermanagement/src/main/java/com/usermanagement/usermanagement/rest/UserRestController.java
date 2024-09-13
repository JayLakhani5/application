package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.*;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.UserResponse;
import com.usermanagement.usermanagement.service.LoginService;
import com.usermanagement.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final LoginService loginService;
    private final JwtClient jwtClient;


    @PostMapping("/user")
    public UserResponse addUser(@RequestBody UserRequest request) {
        return userService.addUser(request);
    }

    @GetMapping("/user")
    public List<UserAndRoleDTO> getAllUsersWithRoles(@RequestHeader("Authorization") String authorizationHeader) {
        return userService.getAllUsersWithRoles(authorizationHeader);
    }

    @GetMapping("/userbyid")
    public UserAndRoleDTO getUserById(@RequestHeader("Authorization") String authorizationHeader) {
        return userService.getUserById(authorizationHeader);
    }

    @PostMapping("/login")
    public String login(@RequestBody Login login) {
        User user = loginService.login(login.getEmailOrContactNumber(), login.getPassword());
        if (user != null) {
            return "Login successful";
        } else {
            return "Invalid credentials";
        }
    }

    @PostMapping("/login2")
    public ResponseEntity<UserResponse> login4(@RequestBody Login login) {
        return loginService.authenticateUser(login.getEmailOrContactNumber(), login.getPassword());
    }

    @PostMapping("/user/{userId}")
    public UserUpdateDTO updateUser(@PathVariable("userId") int id, @RequestBody UserRequest request, @RequestHeader("Authorization") String authorizationHeader) {
        return userService.updateUser(authorizationHeader, id, request);
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
            loginService.logout(sessionId);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }
}

