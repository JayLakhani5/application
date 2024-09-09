package com.Dashboard.Dashboard.controller;



import com.Dashboard.Dashboard.UserServiceClient;
import com.Dashboard.Dashboard.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final UserServiceClient userServiceClient;
    @GetMapping("/user")
    public List<UserResponse> getUsers() {
        return userServiceClient.getUsers();
    }
}

