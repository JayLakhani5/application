package com.Dashboard.Dashboard.service;


import com.Dashboard.Dashboard.UserServiceClient;
import com.Dashboard.Dashboard.response.UserResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Service
public class DashboardService {

    private final UserServiceClient userServiceClient;

    public List<UserResponse> getAllUsers(String token) {
        try {
            return userServiceClient.getAllUsersWithRoles(token);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access: " + e.getMessage(), e);
        } catch (FeignException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), e);
        }
    }
}


