package com.usermanagement.usermanagement.response;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class TokenResponse {
    private String token;
    private long expireTime;
}
