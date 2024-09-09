package com.usermanagement.usermanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateTokenDTO {

    private int userId;
    private List<Integer> roleIds;
    //  private UUID sessionId;
}
