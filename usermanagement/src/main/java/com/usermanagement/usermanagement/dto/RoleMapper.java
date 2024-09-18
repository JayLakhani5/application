package com.usermanagement.usermanagement.dto;

import java.util.Map;

public class RoleMapper {

    private static final Map<Integer, String> roleIdToNameMap = Map.of(
            1, "Admin"
    );

    public static String getRoleName(Integer roleId) {
        return roleIdToNameMap.get(roleId);
    }
}

