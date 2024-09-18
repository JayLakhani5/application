package com.usermanagement.usermanagement.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.usermanagement.usermanagement.dto.RoleMapper;
import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.entity.FileProcessor;
import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserSession;
import com.usermanagement.usermanagement.enums.Roles;
import com.usermanagement.usermanagement.enums.Status;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.FileProcessorRepository;
import com.usermanagement.usermanagement.repository.RoleRepository;
import com.usermanagement.usermanagement.repository.UserRepository;
import com.usermanagement.usermanagement.repository.UserSessionRepository;
import com.usermanagement.usermanagement.validations.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@AllArgsConstructor
@Service
public class FileProcessorService {

    private final UserRepository userRepository;
    private final FileProcessorRepository fileProcessorRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;

    public String processCsvFile(MultipartFile file, String authorizationHeader) {
        Roles roles = Roles.ADMIN;
        String rolesName = roles.getValue();
        Token getToken = new Token();
        getToken.setToken(authorizationHeader);
        TokenValidationResponse tokenResponse = jwtClient.validateToken(getToken);
        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        boolean isAdmin = tokenResponse.getRoleId() != null && tokenResponse.getRoleId().stream()
                .map(RoleMapper::getRoleName)
                .filter(Objects::nonNull).anyMatch(role -> role.equals(rolesName));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you are not admin ");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".csv")) {
            return "Invalid file format. Please upload a CSV file.";
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = reader.readAll();
            if (records.isEmpty()) {
                return "CSV file is empty.";
            }

            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length < 6) {  // Adjusted length check
                    continue;
                }
                String email = record[2];
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    User user = existingUser.get();
                    updateUser(user, record);
                    saveFileProcessor(fileName, Status.Success, "User updated successfully.", user);
                } else {
                    User newUser = createUser(record);
                    userRepository.save(newUser);
                    saveFileProcessor(fileName, Status.Success, "User created successfully.", newUser);
                }
            }
            return "CSV file processed successfully.";
        } catch (IOException | CsvException e) {
            saveFileProcessor(fileName, Status.Error, "Error processing file: " + e.getMessage(), null);
            e.printStackTrace();
            return "Error occurred while processing CSV file.";
        }
    }

    private User createUser(String[] record) {
        if (record.length < 6) {  // Adjusted length check
            throw new IllegalArgumentException("Insufficient data in record");
        }
        ValidationUtils.validatePassword(record[4]);
        ValidationUtils.validateContactNumber(record[3]);
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setFirstName(record[0]);
        user.setLastName(record[1]);
        user.setEmail(record[2]);
        user.setContactNumber(record[3]);
        user.setPassword(ValidationUtils.hashPassword(record[4]));
        user.setCreatedDate(new Date());
        user.setUpdatedDate(null);
        setUserRoles(user, record[5]);
        return user;
    }

    private void updateUser(User user, String[] record) {
        if (record.length < 6) {
            throw new IllegalArgumentException("Insufficient data in record");
        }
        ValidationUtils.validatePassword(record[4]);
        ValidationUtils.validateContactNumber(record[3]);
        user.setFirstName(record[0]);
        user.setLastName(record[1]);
        user.setContactNumber(record[3]);
        user.setPassword(ValidationUtils.hashPassword(record[4]));
        user.setUpdatedDate(new Date());
        setUserRoles(user, record[5]);
        userRepository.save(user);
    }

    private void setUserRoles(User user, String rolesString) {
        String[] roleIds = rolesString.split(",");
        List<Role> roles = new ArrayList<>();
        for (String roleId : roleIds) {
            Optional<Role> role = roleRepository.findById(Integer.parseInt(roleId.trim()));
            role.ifPresent(roles::add);
        }
        user.setRoles(roles);
    }

    private void saveFileProcessor(String fileName, Status status, String reason, User user) {
        Optional<FileProcessor> existingFileProcessor = fileProcessorRepository.findFirstByUserId(user.getId());

        FileProcessor fileProcessor;

        if (existingFileProcessor.isPresent()) {
            fileProcessor = existingFileProcessor.get();
            fileProcessor.setFileName(fileName);
            fileProcessor.setStatus(status);
            fileProcessor.setUpdatedDate(new Date());
            fileProcessor.setReason(reason);
        } else {
            fileProcessor = new FileProcessor();
            fileProcessor.setFileName(fileName);
            fileProcessor.setStatus(status);
            fileProcessor.setCreatedDate(new Date());
            fileProcessor.setUpdatedDate(new Date());
            fileProcessor.setReason(reason);
            fileProcessor.setUser(user);
        }

        fileProcessorRepository.save(fileProcessor);
    }

}
