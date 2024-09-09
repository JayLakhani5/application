package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.dto.GenerateTokenDTO;
import com.usermanagement.usermanagement.response.GenerateTokenDTOResponse;
import com.usermanagement.usermanagement.service.GenerateTokenDTOService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenGenerated {

    private final GenerateTokenDTOService generateTokenDTOService;

    @PostMapping("/find")
    public GenerateTokenDTOResponse findUserROleAndSession(@RequestBody GenerateTokenDTO generateTokenDTO) {
        return generateTokenDTOService.findUserROleAndSession(generateTokenDTO);
    }
}
