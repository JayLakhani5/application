package com.usermanagement.usermanagement.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateTokenDTOResponse {

    private int userId;
    private List<Integer> roleIds;
    //  private UUID sessionId;
}
