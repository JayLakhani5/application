package com.Dashboard.Dashboard.service;


import com.Dashboard.Dashboard.UserServiceClient;
import com.Dashboard.Dashboard.response.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class DashboardService {

    private final UserServiceClient userServiceClient;

    public List<UserResponse> getAllUsers(String token) {
        return userServiceClient.getAllUsersWithRoles(token);
    }
}


