package com.usermanagement.usermanagement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private boolean admin;

}
