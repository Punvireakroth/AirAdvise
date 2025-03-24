package com.example.airadvise.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utility class to handle common form validation functions
 */
object Validator {
    /**
     * Validates if the provided string is a valid email address
     * @param email The email string to validate
     * @return true if valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates if the password meets minimum requirements
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
    
    /**
     * Checks if the password meets strong password requirements
     * (at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char)
     * @param password The password to check
     * @return true if strong, false otherwise
     */
    fun isStrongPassword(password: String): Boolean {
        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        )
        return passwordPattern.matcher(password).matches()
    }
    
    /**
     * Checks if the password and confirm password match
     * @param password The password
     * @param confirmPassword The confirmation password
     * @return true if they match, false otherwise
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
    
    /**
     * Validates if a string is not empty
     * @param input The string to check
     * @return true if not empty, false otherwise
     */
    fun isNotEmpty(input: String): Boolean {
        return input.trim().isNotEmpty()
    }
} 