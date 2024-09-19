package com.usermanagement.usermanagement.request;

import java.util.Date;
import java.util.List;


public record UserRequest(
        String firstName,
        String lastName,
        String email,
        String contactNumber,
        String password,
        boolean admin,
        Date createdDate,
        Date updatedDate,
        List<Integer> roleIds
) {
}

