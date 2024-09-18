package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.UserResponseDTO;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserRoleMapping;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserRoleMappingRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.TokenRequest;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.TokenResponse;
import com.usermanagement.usermanagement.validations.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDemo {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;


    private static UserResponseDTO createUserResponseDTO(User user, TokenResponse token, Set<String> roleNames) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setContactNumber(maskContactNumber(user.getContactNumber()));
        userResponseDTO.setToken(token.getToken());
        userResponseDTO.setExpireTime(token.getExpireTime());
        userResponseDTO.setRoles(roleNames);
        return userResponseDTO;
    }

    private static String maskContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.length() < 10) {
            return contactNumber;
        }
        return contactNumber.substring(0, 2) + "******" + contactNumber.substring(contactNumber.length() - 2);
    }

    public UserResponseDTO addUser(UserRequest userRequest) {
        ValidationUtils.validatePassword(userRequest.password());
        ValidationUtils.validateContactNumber(String.valueOf(userRequest.contactNumber()));

        Optional<User> byEmail = userRepository.findByEmail(userRequest.email());

        User user;
        if (byEmail.isPresent()) {
            user = byEmail.get();
            log.info("Updating user with email: {}", userRequest.email());
            user.setUpdatedDate(new Date());
            if (!Objects.equals(user.getContactNumber(), userRequest.contactNumber())) {
                Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
                if (byContactNumber.isPresent() && !Objects.equals(byContactNumber.get().getId(), user.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number is already in use: " + userRequest.contactNumber());
                }
            }
        } else {
            user = new User();
            user.setCreatedDate(new Date());
            log.info("Create new user with email: {}", userRequest.email());
            Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
            if (byContactNumber.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Contact number is already in use: " + userRequest.contactNumber());
            }
        }

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(ValidationUtils.hashPassword(userRequest.password()));
        user.setContactNumber(userRequest.contactNumber());
        user.setAdmin(true);
        user = userRepository.save(user);

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        if (userRequest.roleIds() == null || userRequest.roleIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role IDs are required.");
        }

        Set<Integer> uniqueRoleIds = new HashSet<>(userRequest.roleIds());
        for (Integer roleId : uniqueRoleIds) {
            Optional<Role> roleById = roleRepository.findById(roleId);
            if (roleById.isEmpty()) {
                throw new RuntimeException("Role with ID " + roleId + " not found.");
            }
            Role role = roleById.get();
            Optional<UserRoleMapping> existingMapping = userRoleMappingRepository.findByUserIdAndRoleId(user, role);
            if (existingMapping.isPresent()) {
                UserRoleMapping mapping = existingMapping.get();
                mapping.setEnable(true);
                mapping.setUpdateDate(new Date());
                userRoleMappingRepository.save(mapping);
            } else {
                UserRoleMapping userRoleMapping = new UserRoleMapping();
                userRoleMapping.setUserId(user);
                userRoleMapping.setRoleId(role);
                userRoleMapping.setEnable(true);
                userRoleMapping.setCreateDate(new Date());
                userRoleMappingRepository.save(userRoleMapping);
            }
        }
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession.setActive(user.isAdmin());
        userSession = userSessionRepository.save(userSession);

        // Generate token
        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()),
                userSession.getSessionId()
        );
        TokenResponse token = jwtClient.createToken(tokenRequest);
        log.info("Generated token: {}", token);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return createUserResponseDTO(user, token, roleNames);
    }


    public ResponseEntity<UserResponseDTO> loginUser(String emailOrContactNumber, String password) {
        User user = userRepository.findByEmailOrContactNumber(emailOrContactNumber);
        if (user == null || !ValidationUtils.checkPassword(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not authorized to log in");
        }
        // Check UserRoleMapping for enable status
        List<UserRoleMapping> roleMappings = userRoleMappingRepository.findByUserId(user);
        for (UserRoleMapping mapping : roleMappings) {
            if (!mapping.getEnable()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User role mapping is disabled. Access denied.");
            }
        }
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession.setActive(true);
        userSession = userSessionRepository.save(userSession);

        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()),
                userSession.getSessionId()
        );
        TokenResponse token = jwtClient.createToken(tokenRequest);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        UserResponseDTO userResponseDTO = createUserResponseDTO(user, token, roleNames);

        return ResponseEntity.ok(userResponseDTO);
    }

    public void logout(UUID sessionId) {
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);
        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid or already inactive");
        }
        userSession.setActive(false);
        userSession.setUpdateDate(new Date());
        userSessionRepository.save(userSession);
    }
}