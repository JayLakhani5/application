package com.usermanagement.usermanagement.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
@Slf4j
public class CsvFileService {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$"
    );
    private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");
    private final UserRepository userRepository;
    private final FileProcessorRepository fileProcessorRepository;
    private final RoleRepository roleRepository;
    private final JwtClient jwtClient;
    private final UserSessionRepository userSessionRepository;

    // Use virtual thread executor for I/O-bound tasks
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public String processCsvFile(MultipartFile file, String authorizationHeader) {

        Roles roles = Roles.ADMIN;
        Token getToken = new Token();
        getToken.setToken(authorizationHeader);
        TokenValidationResponse tokenResponse = jwtClient.validateToken(getToken);
        UUID sessionId = UUID.fromString(tokenResponse.getSessionId());
        UserSession userSession = userSessionRepository.findBySessionId(sessionId);

        if (userSession == null || !userSession.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid");
        }
        if (tokenResponse.getRoleId() == null || !tokenResponse.getRoleId().contains(roles.getValue())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin");
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

            // Submit tasks for each record
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length < 7) {
                    continue;
                }
                futures.add(executor.submit(() -> processRecord(record, fileName)));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get(); // Ensure exceptions are propagated
                } catch (Exception e) {
                    saveFileProcessor(fileName, Status.Error, "Error processing file: " + e.getMessage(), null);
                    e.printStackTrace();
                    return "Error occurred while processing CSV file.";
                }
            }

            return "CSV file processed successfully.";
        } catch (IOException | CsvException e) {
            saveFileProcessor(fileName, Status.Error, "Error processing file: " + e.getMessage(), null);
            e.printStackTrace();
            return "Error occurred while processing CSV file.";
        }
    }

    private void processRecord(String[] record, String fileName) {
        try {
            log.info("CSV process record..");
            String email = record[2];
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                updateUser(user, record);
                saveFileProcessor(fileName, Status.Success, "User updated successfully.", user);
            } else {
                User newUser = createUser(record);
                log.info("new user => {}", newUser);
                userRepository.save(newUser);
                saveFileProcessor(fileName, Status.Success, "User created successfully.", newUser);

            }
        } catch (Exception e) {
            saveFileProcessor(fileName, Status.Error, "Error processing record: " + e.getMessage(), null);
            e.printStackTrace();
        }
    }

    private User createUser(String[] record) {
        if (record.length < 7) {
            throw new IllegalArgumentException("Insufficient data in record");
        }
        if (!isPasswordValid(record[4])) {
            throw new RuntimeException("Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
        if (!isContactNumberValid(String.valueOf(record[3]))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number must be exactly 10 digits.");
        }
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setFirstName(record[0]);
        user.setLastName(record[1]);
        user.setEmail(record[2]);
        user.setContactNumber(record[3]);
        user.setPassword(hashPassword(record[4]));
        user.setAdmin(Boolean.parseBoolean(record[5]));
        user.setCreatedDate(new Date());
        setUserRoles(user, record[6]);
        if (user == null) {
            throw new RuntimeException("User creation failed.");
        }
        return user;
    }


    private void updateUser(User user, String[] record) {
        if (record.length < 7) {
            throw new IllegalArgumentException("Insufficient data in record");
        }
        if (!isPasswordValid(record[4])) {
            throw new RuntimeException("Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
        if (!isContactNumberValid(record[3])) {
            throw new RuntimeException("Contact number must be exactly 10 digits.");
        }
        user.setFirstName(record[0]);
        user.setLastName(record[1]);
        user.setContactNumber(record[3]);
        user.setPassword(hashPassword(record[4]));
        user.setAdmin(Boolean.parseBoolean(record[5]));
        user.setUpdatedDate(new Date());
        setUserRoles(user, record[6]);
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
        if (user == null) {
            log.warn("Cannot save file processor record: user is null");
            return; // Exit early if user is null
        }
        System.out.println("user id ->" + user.getId());
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

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean isContactNumberValid(String contactNumber) {
        return CONTACT_NUMBER_PATTERN.matcher(contactNumber).matches();
    }
}
