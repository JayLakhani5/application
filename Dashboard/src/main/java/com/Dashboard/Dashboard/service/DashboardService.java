package com.Dashboard.Dashboard.service;


import com.Dashboard.Dashboard.UserServiceClient;
import com.Dashboard.Dashboard.exception.CustomException;
import com.Dashboard.Dashboard.response.UserResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class DashboardService {

    private final UserServiceClient userServiceClient;

    public List<UserResponse> getAllUsers(String token) {
        if (Objects.equals(token, "")) {
            throw new CustomException("Token is required", HttpStatus.UNAUTHORIZED);
        }

        try {
            return userServiceClient.getAllUsersWithRoles(token);
        } catch (FeignException.Unauthorized e) {
            throw new CustomException("Unauthorized access: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (FeignException.Forbidden e) {
            throw new CustomException("Access forbidden: " + e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            throw new CustomException("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}