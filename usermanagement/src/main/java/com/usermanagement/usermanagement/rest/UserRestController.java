package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.Login;
import com.usermanagement.usermanagement.dto.UserAndRoleDTO;
import com.usermanagement.usermanagement.dto.UserUpdateDTO;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.UserResponse;
import com.usermanagement.usermanagement.service.LoginService;
import com.usermanagement.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/pass")
    public String checkPassword() {
        return userService.checkPassword();
    }

    @GetMapping("/user")
    public List<UserAndRoleDTO> getAllUsersWithRoles() {
        return userService.getAllUsersWithRoles();
    }

    @GetMapping("/user/{id}")
    public UserAndRoleDTO getUserById(@PathVariable("id") int id) {
        return userService.getUserById(id);
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
    public String logout(@RequestHeader("Authorization") String authorizationHeader) {
        return jwtClient.logout(authorizationHeader);
    }

}

