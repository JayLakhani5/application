package com.usermanagement.usermanagement.validations;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$"
    );
    private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");

    public static void validatePassword(String password) {
        if (!isPasswordValid(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 9 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
    }

    public static void validateContactNumber(String contactNumber) {
        if (!isContactNumberValid(contactNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact number must be exactly 10 digits.");
        }
    }

    private static boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private static boolean isContactNumberValid(String contactNumber) {
        return CONTACT_NUMBER_PATTERN.matcher(contactNumber).matches();
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
