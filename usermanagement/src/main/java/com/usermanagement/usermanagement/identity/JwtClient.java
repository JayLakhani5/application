package com.usermanagement.usermanagement.identity;

import com.usermanagement.usermanagement.request.TokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "Identity", url = "http://localhost:8082")
public interface JwtClient {
    @PostMapping("/generate-token")
    String generateToken(@RequestBody TokenRequest reuest);

    @GetMapping("/extractUserId")
    public Integer extractUserId(@RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authorizationHeader);
}