package com.usermanagement.usermanagement.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RoleResponse {

    private int id;
    private String uuid;
    private String roleName;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
}

