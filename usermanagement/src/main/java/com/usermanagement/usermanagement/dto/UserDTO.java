package com.usermanagement.usermanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private int contactNumber;
    private boolean admin;

}
