package com.usermanagement.usermanagement.response;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private int id;
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String email;
    private int contactNumber;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
    private List<RoleResponse> roles;

    private String token;
    private String password;


}

