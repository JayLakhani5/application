package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.UserAndRoleDTO;
import com.usermanagement.usermanagement.dto.UserUpdateDTO;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping("/user")
    public List<UserAndRoleDTO> getAllUsersWithRoles(@RequestHeader("Authorization") String authorizationHeader) {
        return userService.getAllUsersWithRoles(authorizationHeader);
    }

    @GetMapping("/userbyid")
    public UserAndRoleDTO getUserById(@RequestHeader("Authorization") String authorizationHeader) {
        return userService.getUserById(authorizationHeader);
    }

    @PostMapping("/user/{userId}")
    public UserUpdateDTO updateUser(@PathVariable("userId") int id, @RequestBody UserRequest request, @RequestHeader("Authorization") String authorizationHeader) {
        return userService.updateUser(authorizationHeader, id, request);
    }

}