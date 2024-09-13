package com.usermanagement.usermanagement.identity;

import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.request.TokenRequest;
import com.usermanagement.usermanagement.response.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "Identity", url = "http://localhost:8082")
public interface JwtClient {
    @PostMapping("generate-token")
    public TokenResponse createToken(@RequestBody TokenRequest request);

    @PostMapping("/validate-token")
    public TokenValidationResponse validateToken(@RequestBody Token token);


}