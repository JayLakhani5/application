package com.Identity.Identity.rest;

import com.Identity.Identity.request.TokenRequest;
import com.Identity.Identity.response.TokenResponse;
import com.Identity.Identity.response.TokenValidationResponse;
import com.Identity.Identity.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IdentityController {

    private final TokenService tokenService;

    @PostMapping("generate-token")
    public TokenResponse createToken(@RequestBody TokenRequest request) {
        return tokenService.generateToken(request);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenResponse tokenResponse) {
        TokenValidationResponse response = tokenService.validateToken(tokenResponse.getToken());

        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
