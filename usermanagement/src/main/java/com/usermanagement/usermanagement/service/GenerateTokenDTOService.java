package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.GenerateTokenDTO;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.response.GenerateTokenDTOResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GenerateTokenDTOService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    private static GenerateTokenDTOResponse getGenerateTokenDTOResponse(GenerateTokenDTO generateTokenDTO, User user) {
        List<Integer> userRoleIds = new ArrayList<>();
        for (Role role : user.getRoles()) {
            userRoleIds.add(role.getId());
        }

        List<Integer> requestedRoleIds = generateTokenDTO.getRoleIds();
        for (Integer roleId : requestedRoleIds) {
            if (!userRoleIds.contains(roleId)) {
                throw new RuntimeException("Role ID does not match for user ID: " + generateTokenDTO.getUserId());
            }
        }
        GenerateTokenDTOResponse response = new GenerateTokenDTOResponse();
        response.setUserId(user.getId());
        response.setRoleIds(userRoleIds);
        return response;
    }

    public GenerateTokenDTOResponse findUserROleAndSession(GenerateTokenDTO generateTokenDTO) {
        Optional<User> userOptional = userRepository.findById(generateTokenDTO.getUserId());

        System.out.println("user id is " + userOptional);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User ID does not match: " + generateTokenDTO.getUserId());
        }
        User user = userOptional.get();
        return getGenerateTokenDTOResponse(generateTokenDTO, user);

    }

}
