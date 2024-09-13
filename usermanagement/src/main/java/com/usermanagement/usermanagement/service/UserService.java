package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.*;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.enums.Roles;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.TokenRequest;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import com.usermanagement.usermanagement.response.TokenResponse;
import com.usermanagement.usermanagement.response.UserResponse;
import com.usermanagement.usermanagement.validations.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;


    public List<UserAndRoleDTO> getAllUsersWithRoles(String authHeader) {

        Roles roles = Roles.ADMIN;
        int adminRole = roles.getValue();
        Token getToken = new Token();
        getToken.setToken(authHeader);
        TokenValidationResponse tokenResponse = jwtClient.validateToken(getToken);
        if (tokenResponse.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token is expired");
        }
        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        if (tokenResponse.getRoleId() == null || !tokenResponse.getRoleId().contains(adminRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin");
        }

        List<User> users = userRepository.findAll();
        List<UserAndRoleDTO> userAndRoleDTOs = new ArrayList<>();

        for (User user : users) {
            UserAndRoleDTO dto = new UserAndRoleDTO();
            dto.setUser(convertToUserDTO(user));
            dto.setRoles(user.getRoles());
            userAndRoleDTOs.add(dto);
        }
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

        Optional<User> optionalUser = userRepository.findById(validationResponse.getUserId());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserAndRoleDTO dto = new UserAndRoleDTO();
            dto.setUser(convertToUserDTO(user));
            dto.setRoles(user.getRoles());
            return dto;
        } else {
            throw new RuntimeException("User not found with ID: " + validationResponse.getUserId());
        }
    }


    public UserResponse addUser(UserRequest userRequest) {
        // Validate user request data
        ValidationUtils.validatePassword(userRequest.password());
        ValidationUtils.validateContactNumber(String.valueOf(userRequest.contactNumber()));
        Optional<User> byEmail = userRepository.findByEmail(userRequest.email());
        if (byEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use: " + userRequest.email());
        }
        Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
        if (byContactNumber.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This contact number is already in use: " + userRequest.contactNumber());
        }
        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(ValidationUtils.hashPassword(userRequest.password()));
        user.setContactNumber(userRequest.contactNumber());
        user.setCreatedDate(new Date());
        user.setAdmin(userRequest.admin());
        for (Integer roleId : userRequest.roleIds()) {
            Optional<Role> roleById = roleRepository.findById(roleId);
            if (roleById.isEmpty()) {
                throw new RuntimeException("Role with ID " + roleId + " not found.");
            }
            Role role = roleById.get();
            user.addRole(role);
        }
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession.setActive(user.isAdmin());
        user = userRepository.save(user);
        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                user.getRoles().stream().map(Role::getId).toList(),
                userSession.getSessionId()
        );

        TokenResponse token = jwtClient.createToken(tokenRequest);
        userSessionRepository.save(userSession);
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .admin(user.isAdmin())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .password(user.getPassword())
                .build();
        if (user.getRoles() != null) {
            List<RoleResponse> roleResponses = new ArrayList<>();
            for (Role role : user.getRoles()) {
                RoleResponse roleResponse = new RoleResponse();
                roleResponse.setId(role.getId());
                roleResponse.setUuid(role.getUuid().toString());
                roleResponse.setRoleName(role.getRoleName());
                roleResponse.setAdmin(role.isAdmin());
                roleResponse.setCreatedDate(role.getCreatedDate());
                roleResponse.setUpdatedDate(role.getUpdatedDate());
                roleResponses.add(roleResponse);


            }
            userResponse.setRoles(roleResponses);
            userResponse.setPassword(user.getPassword());

        }
        userResponse.setToken(token.getToken());
        return userResponse;
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

        // Validate user request data
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

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .admin(user.isAdmin())
                .updateDate(user.getUpdatedDate())
                .build();
        return userUpdateDTO;
    }
}



