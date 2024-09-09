package com.Dashboard.Dashboard;


import com.Dashboard.Dashboard.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "usermanagement", url = "http://localhost:8081")
public interface UserServiceClient {
    @GetMapping("/user")
    List<UserResponse> getUsers();
}


