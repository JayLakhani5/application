package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.request.TokenRequest;
import com.usermanagement.usermanagement.response.RoleResponse;
import com.usermanagement.usermanagement.response.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserSessionRepository userSessionRepository;
    private final JwtClient jwtClient;

    public User login(String emailOrContactNumber, String password) {
        return userRepository.findByEmailOrContactNumberAndPassword(emailOrContactNumber, password);
    }


    public ResponseEntity<UserResponse> authenticateUser(String emailOrContactNumber, String password) {
        User user = userRepository.findByEmailOrContactNumber(emailOrContactNumber);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        }
        List<Integer> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        System.out.println("datttaaaa" + roleIds);

        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreatedDate(new Date());
        userSession = userSessionRepository.save(userSession);


        TokenRequest tokenRequest = new TokenRequest(
                user.getId(),
                roleIds,
                userSession.getSessionId()
        );
        String token = jwtClient.generateToken(tokenRequest);

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
                .token(token)
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
        }

        return ResponseEntity.ok(userResponse);
    }

}

