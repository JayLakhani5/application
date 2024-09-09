package com.Dashboard.Dashboard.service;



import com.Dashboard.Dashboard.UserServiceClient;
import com.Dashboard.Dashboard.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private UserServiceClient userServiceClient;

    public List<UserResponse> getAllUsers() {
        return userServiceClient.getUsers();
    }
}


