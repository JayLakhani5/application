package com.Dashboard.Dashboard.controller;


import com.Dashboard.Dashboard.response.UserResponse;
import com.Dashboard.Dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/user")
    public List<UserResponse> getUsers(@RequestHeader(AUTHORIZATION) String token) {
        return dashboardService.getAllUsers(token);
    }
}

