package com.Identity.Identity.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TokenValidationResponse {
    private Integer userId;
    private List<Integer> roleId;
    private UUID sessionId;
    private boolean valid;
}

