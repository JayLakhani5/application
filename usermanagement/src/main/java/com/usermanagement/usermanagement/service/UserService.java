package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.UserAndRoleDTO;
import com.usermanagement.usermanagement.dto.UserDTO;
import com.usermanagement.usermanagement.dto.UserUpdateDTO;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.TokenRequest;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import com.usermanagement.usermanagement.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$"
    );
    private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;


    public List<UserAndRoleDTO> getAllUsersWithRoles() {
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


    public UserAndRoleDTO getUserById(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserAndRoleDTO dto = new UserAndRoleDTO();
            dto.setUser(convertToUserDTO(user));
            dto.setRoles(user.getRoles());
            return dto;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }


    public UserResponse addUser(UserRequest userRequest) {
        if (isPasswordValid(userRequest.password())) {
            throw new RuntimeException("Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
        if (isContactNumberValid(String.valueOf(userRequest.contactNumber()))) {
            throw new RuntimeException("Contact number must be exactly 10 digits.");
        }
        Optional<User> byEmail = userRepository.findByEmail(userRequest.email());
        if (byEmail.isPresent()) {
            throw new RuntimeException("this email is already inserted " + userRequest.email());
        }
        Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
        if (byContactNumber.isPresent()) {
            throw new RuntimeException("this contactNumber is already inserted " + userRequest.contactNumber());
        }
        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(hashPassword(userRequest.password()));
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

        String token = jwtClient.generateToken(tokenRequest);
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
        userResponse.setToken(token);
        return userResponse;
    }

    private boolean isContactNumberValid(String contactNumber) {
        return !CONTACT_NUMBER_PATTERN.matcher(contactNumber).matches();
    }

    private boolean isPasswordValid(String password) {
        return !PASSWORD_PATTERN.matcher(password).matches();
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String checkPassword() {
        String hashedPassword = "$2a$10$c8eb9aEykni1tIcKqYgOp.7/1CEmkoxYJtb4dx4kEHfBtbSBEAkUW";
        String candidatePassword = "Yash@1234";

        if (BCrypt.checkpw(candidatePassword, hashedPassword)) {
            return "password match";
        } else {
            return "password not match";
        }
    }

    public UserUpdateDTO updateUser(String authHeader, int userId, UserRequest userRequest) {

        Integer tokenUserId = jwtClient.extractUserId(authHeader);


        if (!tokenUserId.equals(userId)) {
            throw new RuntimeException("Unauthorized access: Token does not match user ID.");
        }

        Optional<User> existingUserOpt = userRepository.findById(userId);
        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with id " + userId);
        }

        User user = existingUserOpt.get();

        if (isPasswordValid(userRequest.password())) {
            throw new RuntimeException("Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
        if (isContactNumberValid(String.valueOf(userRequest.contactNumber()))) {
            throw new RuntimeException("Contact number must be exactly 10 digits.");
        }

        if (!user.getEmail().equals(userRequest.email())) {
            Optional<User> byEmail = userRepository.findByEmail(userRequest.email());
            if (byEmail.isPresent()) {
                throw new RuntimeException("Email is already in use: " + userRequest.email());
            }
        }

        if (!Objects.equals(user.getContactNumber(), userRequest.contactNumber())) {
            Optional<User> byContactNumber = userRepository.findByContactNumber(userRequest.contactNumber());
            if (byContactNumber.isPresent() && byContactNumber.get().getId() != userId) {
                throw new RuntimeException("Contact number is already in use: " + userRequest.contactNumber());
            }
        }

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(hashPassword(userRequest.password()));
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



