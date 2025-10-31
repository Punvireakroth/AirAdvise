package com.example.airadvise.extensions

import android.content.Context
import com.example.airadvise.R
import com.example.airadvise.utils.Validator
import com.google.android.material.textfield.TextInputLayout

/**
 * Extension functions for form validation with TextInputLayout
 */

/**
 * Validate email field and set error if invalid
 * @return true if valid, false otherwise
 */
fun TextInputLayout.validateEmail(context: Context): Boolean {
    val email = editText?.text.toString().trim()
    
    // Reset error
    error = null
    
    // Check if empty
    if (!Validator.isNotEmpty(email)) {
        error = context.getString(R.string.email_required)
        return false
    }
    
    // Check if valid email format
    if (!Validator.isValidEmail(email)) {
        error = context.getString(R.string.invalid_email)
        return false
    }
    
    return true
}

/**
 * Validate password field and set error if invalid
 * @return true if valid, false otherwise
 */
fun TextInputLayout.validatePassword(context: Context): Boolean {
    val password = editText?.text.toString()
    
    // Reset error
    error = null
    
    // Check if empty
    if (!Validator.isNotEmpty(password)) {
        error = context.getString(R.string.password_required)
        return false
    }
    
    // Check if valid password
    if (!Validator.isValidPassword(password)) {
        error = context.getString(R.string.password_too_short)
        return false
    }
    
    return true
}

/**
 * Validate that field is not empty
 * @param fieldName The name of the field for error message
 * @return true if valid, false otherwise
 */
fun TextInputLayout.validateNotEmpty(context: Context, errorMessageResId: Int): Boolean {
    val text = editText?.text.toString().trim()
    
    // Reset error
    error = null
    
    if (text.isEmpty()) {
        error = context.getString(errorMessageResId)
        return false
    }
    
    return true
}

/**
 * Clear error on this TextInputLayout
 */
fun TextInputLayout.clearError() {
    error = null
}
