package com.Dashboard.Dashboard.response;

import com.Dashboard.Dashboard.dto.UserDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {
    private UserDTO user;
    private List<RoleResponse> roles;
}
