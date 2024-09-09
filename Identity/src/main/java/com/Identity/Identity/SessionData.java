package com.Identity.Identity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SessionData {
    private final Integer userId;
    private final List<Integer> roleId;
    private final Date createdAt;
}
