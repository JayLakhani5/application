package com.usermanagement.usermanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponseDTO {

    private String token;
    private long expireTime;
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private List<String> roles;
}
