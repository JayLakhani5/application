package com.usermanagement.usermanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles {
    ADMIN(8);
    private final int value;
}
