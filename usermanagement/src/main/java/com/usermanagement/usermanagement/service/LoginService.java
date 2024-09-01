package com.usermanagement.usermanagement.service;

import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class LoginService {

    private final UserRepository userRepository;

    public User login(String emailOrContactNumber, String password) {
        return userRepository.findByEmailOrContactNumberAndPassword(emailOrContactNumber, password);
    }
}

