package com.siemens.internship;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Email validator class that validates email addresses.
 * It uses a regex pattern and provides methods to check if an email is valid.
 */
@Component
public class EmailValidator {

    // Email regex pattern based on RFC 5322 standard
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * Validates an email address.
     *
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        if (email.length() > 40) {
            return false; // Email is too long
        }

        if (email.length() < 5) {
            return false; // Email is too short
        }

        if (email.contains("..")) {
            return false;  // Email cannot contain consecutive dots
        }

        if (!pattern.matcher(email).matches()) {
            return false; // Email does not match the regex pattern
        }
        if (email.startsWith(".") || email.endsWith(".")) {
            return false; // Email cannot start or end with a dot
        }
        return true;
    }
}