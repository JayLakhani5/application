package com.usermanagement.usermanagement.service;


import com.usermanagement.usermanagement.dto.RoleMapper;
import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.enums.Roles;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.RoleRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;


    public RoleResponse addOrUpdateRole(RoleRequest request, String authHeader) {
        if (Objects.equals(authHeader, "")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token is required");
        }
        Roles roles = Roles.ADMIN;
        String adminRole = roles.getValue();
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse tokenResponse;
        try {
            tokenResponse = jwtClient.validateToken(getToken);
        } catch (FeignException e) {
            if (e.status() == 401) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is invalid or malformed");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating token");
            }
        }
        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        boolean isAdmin = tokenResponse.getRoleId() != null && tokenResponse.getRoleId().stream()
                .map(RoleMapper::getRoleName)
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equals(adminRole));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin");
        }
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
