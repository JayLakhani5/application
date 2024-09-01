package com.usermanagement.usermanagement.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private int contactNumber;
    private String password;
    private boolean admin;
    private Date createdDate;
    private Date updatedDate;
    private List<Integer> roleIds;

}

