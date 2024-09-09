package com.usermanagement.usermanagement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class UserUpdateDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private boolean admin;
    private Date updateDate;

}
