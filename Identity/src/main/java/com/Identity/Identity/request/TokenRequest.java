package com.Identity.Identity.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TokenRequest {
    private Integer userId;
    private List<Integer> roleId;
    private UUID sessionId;
}
