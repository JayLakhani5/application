package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.dto.UserAndRoleDTO;
import com.usermanagement.usermanagement.dto.UserDTO;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.jwt.JwtUtil;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.request.UserRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import com.usermanagement.usermanagement.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$"
    );
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setContactNumber(user.getContactNumber());
        dto.setAdmin(user.isAdmin());
        return dto;
    }


    public UserAndRoleDTO getUserById(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserAndRoleDTO dto = new UserAndRoleDTO();
            dto.setUser(convertToUserDTO(user));
            dto.setRoles(user.getRoles()); // Fetch roles from the user entity
            return dto;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }


    public UserResponse addUser(UserRequest userRequest) {
        if (!isPasswordValid(userRequest.getPassword())) {
            throw new RuntimeException("Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(hashPassword(userRequest.getPassword()));
        user.setContactNumber(userRequest.getContactNumber());
        user.setCreatedDate(new Date());
        user.setAdmin(userRequest.isAdmin());
        // Fetch and set roles
        for (Integer roleId : userRequest.getRoleIds()) {
            Optional<Role> roleById = roleRepository.findById(roleId);
            if (roleById.isEmpty()) {
                throw new RuntimeException("Role with ID " + roleId + " not found.");
            }
            Role role = roleById.get();
            user.addRole(role);
        }
        // Save user and roles
        user = userRepository.save(user);
        //create token
        String token = JwtUtil.generateToken(user.getEmail());
        // Convert to UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUuid(user.getUuid());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setContactNumber(user.getContactNumber());
        userResponse.setAdmin(user.isAdmin());
        userResponse.setCreatedDate(user.getCreatedDate());
        userResponse.setUpdatedDate(user.getUpdatedDate());

        // Convert roles to RoleResponse
        if (user.getRoles() != null) {
            List<RoleResponse> roleResponses = new ArrayList<>();
            for (Role role : user.getRoles()) {
                RoleResponse roleResponse = new RoleResponse();
                roleResponse.setId(role.getId());
                roleResponse.setUuid(role.getUuid());
                roleResponse.setRoleName(role.getRoleName());
                roleResponse.setAdmin(role.isAdmin());
                roleResponse.setCreatedDate(role.getCreatedDate());
                roleResponse.setUpdatedDate(role.getUpdatedDate());
                roleResponses.add(roleResponse);


            }
            userResponse.setRoles(roleResponses);
            userResponse.setPassword(user.getPassword());
            System.out.println("password is" + userResponse.getPassword());
        }
        userResponse.setToken(token);
        return userResponse;
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }


    public String checkPassword() {
        String hashedPassword = "$2a$10$c8eb9aEykni1tIcKqYgOp.7/1CEmkoxYJtb4dx4kEHfBtbSBEAkUW";
        String candidatePassword = "Yash@1234"; // The password you want to check

        if (BCrypt.checkpw(candidatePassword, hashedPassword)) {
            return "password match";
        } else {
            return "password not match";
        }
    }
}



