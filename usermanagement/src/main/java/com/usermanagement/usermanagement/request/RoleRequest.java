package com.usermanagement.usermanagement.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoleRequest {
    private String roleName;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
    private List<Integer> userIds;
}
