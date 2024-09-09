package com.Identity.Identity.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TokenValidationResponse {
    private Integer userId;
    private List<Integer> roleId;
    private String sessionId;
    private boolean valid;
}

