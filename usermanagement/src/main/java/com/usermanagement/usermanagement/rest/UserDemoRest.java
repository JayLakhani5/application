package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.Login;
import com.usermanagement.usermanagement.dto.UserResponseDTO;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.service.UserDemo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserDemoRest {

    private final UserDemo userDemo;


    @PostMapping("/userdemo")
    public UserResponseDTO addUser(@RequestBody UserRequest userRequest) {
        return userDemo.addUser(userRequest);
    }

    @PostMapping("/login22")
    public ResponseEntity<UserResponseDTO> login4(@RequestBody Login login) {
        return userDemo.loginUser(login.getEmailOrContactNumber(), login.getPassword());
    }
}
