package com.usermanagement.usermanagement.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private Integer contactNumber; // Use Integer to allow null values if the contact number is not provided
    private String password;


}
