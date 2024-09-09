package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.request.RoleRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import com.usermanagement.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RoleController {
    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @GetMapping("/role")
    public List<Role> allRole() {
        return roleRepository.findAll();
    }

    @PostMapping("/role")
    public RoleResponse addRole(@RequestBody RoleRequest request) {
        return roleService.addOrUpdateRole(request);
    }


}
