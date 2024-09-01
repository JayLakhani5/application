package com.usermanagement.usermanagement.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RoleResponse {

    private int id;
    private UUID uuid;
    private String roleName;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
    private List<UserResponse> users;
}

