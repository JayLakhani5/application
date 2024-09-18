package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.*;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.enums.Roles;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.validations.ValidationUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;


    public List<UserAndRoleDTO> getAllUsersWithRoles(String authHeader) {
        Roles roles = Roles.ADMIN;
        String adminRole = roles.getValue();
        Token getToken = new Token();
        getToken.setToken(authHeader);

        TokenValidationResponse tokenResponse;
        try {
            tokenResponse = jwtClient.validateToken(getToken);
            if (tokenResponse.isExpired()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is expired");
            }
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), e);
        }

        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }

        boolean isAdmin = tokenResponse.getRoleId() != null &&
                tokenResponse.getRoleId().stream()
                        .map(RoleMapper::getRoleName)
                        .filter(Objects::nonNull)
                        .anyMatch(role -> role.equals(adminRole));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin");
        }

        List<User> users = userRepository.findAll();
        List<UserAndRoleDTO> userAndRoleDTOs = users.stream()
                .map(user -> {
                    UserAndRoleDTO dto = new UserAndRoleDTO();
                    dto.setUser(convertToUserDTO(user));
                    dto.setRoles(user.getRoles());
                    return dto;
                })
                .collect(Collectors.toList());

        return userAndRoleDTOs;
    }

    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .admin(user.isAdmin())
                .build();
    }


    public UserAndRoleDTO getUserById(String authHeader) {
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse validationResponse = jwtClient.validateToken(getToken);
        UUID sessionId = UUID.fromString(validationResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        if (!validationResponse.getUserId().equals(validationResponse.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access: Token does not match user ID.");
        }
        if (validationResponse.isExpired()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "token is expire");
        }
        Optional<User> optionalUser = userRepository.findById(validationResponse.getUserId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserAndRoleDTO dto = new UserAndRoleDTO();
            dto.setUser(convertToUserDTO(user));
            dto.setRoles(user.getRoles());
            return dto;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + validationResponse.getUserId());
        }
    }

    public UserUpdateDTO updateUser(String authHeader, int userId, UserRequest userRequest) {
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse validationResponse = jwtClient.validateToken(getToken);
        UUID sessionId = UUID.fromString(validationResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        if (!validationResponse.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access: Token does not match user ID.");
        }
        Optional<User> existingUserOpt = userRepository.findById(userId);
        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with id " + userId);
        }
        User user = existingUserOpt.get();

        ValidationUtils.validatePassword(userRequest.password());
        ValidationUtils.validateContactNumber(String.valueOf(userRequest.contactNumber()));

        if (!user.getEmail().equals(userRequest.email())) {
            Optional<User> byEmail = userRepository.findByEmail(userRequest.email());
            if (byEmail.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use: " + userRequest.email());
            }
        }

        if (!Objects.equals(user.getContactNumber(), userRequest.contactNumber())) {
            Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
            if (byContactNumber.isPresent() && byContactNumber.get().getId() != userId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number is already in use: " + userRequest.contactNumber());
            }
        }

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(ValidationUtils.hashPassword(userRequest.password()));
        user.setContactNumber(userRequest.contactNumber());
        user.setAdmin(userRequest.admin());
        user.setUpdatedDate(new Date());
        userRepository.save(user);

        return UserUpdateDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .admin(user.isAdmin())
                .updateDate(user.getUpdatedDate())
                .build();
    }
}



