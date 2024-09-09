package com.usermanagement.usermanagement.service;


import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.request.RoleRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleResponse addOrUpdateRole(RoleRequest request) {
        Optional<Role> existingRoleOptional = roleRepository.findByRoleName(request.getRoleName());
        Role role;
        if (existingRoleOptional.isPresent()) {
            role = existingRoleOptional.get();
            role.setAdmin(request.isAdmin());
            role.setUpdatedDate(new Date());
        } else {
            role = new Role();
            role.setRoleName(request.getRoleName());
            role.setAdmin(request.isAdmin());
            role.setUuid(UUID.randomUUID());
            role.setCreatedDate(new Date());
        }

        role = roleRepository.save(role);
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(role.getId());
        roleResponse.setRoleName(role.getRoleName());
        roleResponse.setAdmin(role.isAdmin());
        roleResponse.setCreatedDate(role.getCreatedDate());
        roleResponse.setUuid(role.getUuid().toString());
        return roleResponse;
    }
}
