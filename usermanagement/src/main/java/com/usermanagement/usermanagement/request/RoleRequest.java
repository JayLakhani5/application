package com.usermanagement.usermanagement.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RoleRequest {
    private String roleName;
    private UUID uuid;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
    private List<Integer> userIds;
}
