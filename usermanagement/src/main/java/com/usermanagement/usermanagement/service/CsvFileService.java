package com.usermanagement.usermanagement.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.usermanagement.usermanagement.dto.RoleMapper;
import com.usermanagement.usermanagement.dto.Token;
import com.usermanagement.usermanagement.dto.TokenValidationResponse;
import com.usermanagement.usermanagement.entity.*;
import com.usermanagement.usermanagement.enums.Roles;
import com.usermanagement.usermanagement.enums.Status;
import com.usermanagement.usermanagement.identity.JwtClient;
import com.usermanagement.usermanagement.repository.*;
import com.usermanagement.usermanagement.validations.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AllArgsConstructor
@Service
@Slf4j
public class CsvFileService {

    private final UserRepository userRepository;
    private final FileProcessorRepository fileProcessorRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Transactional
    public String processCsvFile(MultipartFile file, String authorizationHeader) {
        // Validate token
        Token getToken = new Token();
        getToken.setToken(authorizationHeader);
        TokenValidationResponse tokenResponse = jwtClient.validateToken(getToken);
        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }

        // Check if the user has admin rights
        Roles roles = Roles.ADMIN;
        String adminRole = roles.getValue();
        boolean isAdmin = tokenResponse.getRoleId() != null && tokenResponse.getRoleId().stream()
                .map(RoleMapper::getRoleName)
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equals(adminRole));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin");
        }

        // Validate file
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".csv")) {
            return "Invalid file format. Please upload a CSV file.";
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = reader.readAll();
            if (records.isEmpty()) {
                return "CSV file is empty.";
            }

            // Process records using virtual threads
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) { // Skip header
                String[] record = records.get(i);
                if (record.length < 6) {
                    saveFileProcessor(file.getOriginalFilename(), Status.Error, "Incomplete record at line " + (i + 1), null);
                    continue;
                }
                tasks.add(createTask(record, file.getOriginalFilename()));
            }

            // Invoke tasks
            List<String> results = executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("Error getting result from future: ", e);
                            return "Failure";
                        }
                    })
                    .toList();

            // Check results
            if (results.contains("Failure")) {
                return "Some records could not be processed.";
            }

            return "CSV file processed successfully.";
        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file: ", e);
            saveFileProcessor(file.getOriginalFilename(), Status.Error, "Error reading file: " + e.getMessage(), null);
            return "Error occurred while processing CSV file.";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Callable<String> createTask(String[] record, String fileName) {
        return () -> {
            try {
                processRecord(record, fileName);
                return "Success";
            } catch (Exception e) {
                log.error("Error processing record: ", e);
                saveFileProcessor(fileName, Status.Error, "Error processing record: " + e.getMessage(), null);
                return "Failure";
            }
        };
    }

    private void processRecord(String[] record, String fileName) {
        try {
            String email = record[2];
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                updateUser(user, record);
            } else {
                user = createUser(record);
                user = userRepository.save(user);
            }

            // Assuming record[5] contains the role IDs as a comma-separated string
            String[] roleIds = record[5].split(",");
            for (String roleId : roleIds) {
                saveUserRoleMapping(user, roleId);
            }

            log.info("User processed: {}", user);
            saveFileProcessor(fileName, Status.Success, "User processed successfully.", user);
        } catch (Exception e) {
            log.error("Error processing record: ", e);
            saveFileProcessor(fileName, Status.Error, "Error processing record: " + e.getMessage(), null);
        }
    }

    private User createUser(String[] record) {
        if (record.length < 6) {
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
        user.setAdmin(false);
        user.setCreatedDate(new Date());
        user.setUpdatedDate(null);
        setUserRoles(user, record[5]);
        log.info("Created user: {}", user);
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
            Optional<Role> roleOptional = roleRepository.findById(Integer.parseInt(roleId.trim()));
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                roles.add(role); // Role is now managed by the persistence context
            } else {
                log.warn("Role with ID {} not found", roleId.trim());
            }
        }

        user.setRoles(roles);
    }

    private void saveUserRoleMapping(User user, String roleId) {
        Optional<Role> roleOptional = roleRepository.findById(Integer.parseInt(roleId.trim()));
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            // Check if UserRoleMapping already exists
            Optional<UserRoleMapping> existingMapping = userRoleMappingRepository.findByUserIdAndRoleId(user, role);

            if (existingMapping.isPresent()) {
                // Update existing mapping
                UserRoleMapping mapping = existingMapping.get();
                mapping.setEnable(true); // Ensure enable is set to true
                mapping.setUpdateDate(new Date()); // Set the update date
                userRoleMappingRepository.save(mapping);
            } else {
                // Create a new UserRoleMapping
                UserRoleMapping userRoleMapping = new UserRoleMapping();
                userRoleMapping.setUserId(user);
                userRoleMapping.setRoleId(role);
                userRoleMapping.setEnable(true);
                userRoleMapping.setCreateDate(new Date());
                userRoleMapping.setUpdateDate(new Date());
                userRoleMappingRepository.save(userRoleMapping);
            }
        } else {
            log.warn("Role with ID {} not found for user {}", roleId, user.getEmail());
        }
    }

    private void saveFileProcessor(String fileName, Status status, String reason, User user) {
        FileProcessor fileProcessor = new FileProcessor();
        fileProcessor.setFileName(fileName);
        fileProcessor.setStatus(status);
        fileProcessor.setCreatedDate(new Date());
        fileProcessor.setUpdatedDate(new Date());
        fileProcessor.setReason(reason);

        if (user != null) {
            Optional<FileProcessor> existingFileProcessor = fileProcessorRepository.findFirstByUserId(user.getId());
            if (existingFileProcessor.isPresent()) {
                fileProcessor = existingFileProcessor.get();
                fileProcessor.setUpdatedDate(new Date());
            } else {
                fileProcessor.setUser(user);
            }
        }

        fileProcessorRepository.save(fileProcessor);
    }
}
