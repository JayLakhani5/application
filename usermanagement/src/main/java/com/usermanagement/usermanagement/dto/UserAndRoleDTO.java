package com.usermanagement.usermanagement.dto;

import com.usermanagement.usermanagement.entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserAndRoleDTO {
    private UserDTO user;
    private List<Role> roles;
}
