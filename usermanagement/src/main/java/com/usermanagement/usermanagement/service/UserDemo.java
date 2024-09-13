package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.UserResponseDTO;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDemo {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;

    // Utility method to create a UserResponseDTO
    private static UserResponseDTO createUserResponseDTO(User user, TokenResponse token, List<String> roleNames) {
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

    // Utility method to mask the contact number
    private static String maskContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.length() < 4) {
            return contactNumber;
        }
        return contactNumber.substring(0, 2) + "******" + contactNumber.substring(contactNumber.length() - 2);
    }

    // Method to add a new user
    public UserResponseDTO addUser(UserRequest userRequest) {
        // Validate user input
        ValidationUtils.validatePassword(userRequest.password());
        ValidationUtils.validateContactNumber(String.valueOf(userRequest.contactNumber()));

        // Check for existing email
        Optional<User> byEmail = userRepository.findByEmail(userRequest.email());
        if (byEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use: " + userRequest.email());
        }

        // Check for existing contact number
        Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
        if (byContactNumber.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This contact number is already in use: " + userRequest.contactNumber());
        }

        // Create a new user entity
        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(ValidationUtils.hashPassword(userRequest.password()));
        user.setContactNumber(userRequest.contactNumber());
        user.setCreatedDate(new Date());
        user.setAdmin(userRequest.admin());

        // Assign roles to the user
        for (Integer roleId : userRequest.roleIds()) {
            Optional<Role> roleById = roleRepository.findById(roleId);
            if (roleById.isEmpty()) {
                throw new RuntimeException("Role with ID " + roleId + " not found.");
            }
            Role role = roleById.get();
            user.addRole(role);
        }

        // Create and save user session
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession.setActive(user.isAdmin());
        userSession = userSessionRepository.save(userSession);

        // Save the user entity
        user = userRepository.save(user);

        // Generate token for the user
        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()),
                userSession.getSessionId()
        );
        TokenResponse token = jwtClient.createToken(tokenRequest);
        log.info("Generated token: {}", token);

        // Prepare the response DTO
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return createUserResponseDTO(user, token, roleNames);
    }

    // Method to handle user login
    public ResponseEntity<UserResponseDTO> loginUser(String emailOrContactNumber, String password) {
        // Find user by email or contact number
        User user = userRepository.findByEmailOrContactNumber(emailOrContactNumber);

        // Validate user and password
        if (user == null || !ValidationUtils.checkPassword(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not authorized to log in");
        }

        // Create a new user session
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession.setActive(true);
        userSession = userSessionRepository.save(userSession);

        // Generate token for the user
        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toList()),
                userSession.getSessionId()
        );
        TokenResponse token = jwtClient.createToken(tokenRequest);

        // Prepare the response DTO
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        UserResponseDTO userResponseDTO = createUserResponseDTO(user, token, roleNames);

        return ResponseEntity.ok(userResponseDTO);
    }
}
