package com.example.airadvise.utils

import android.content.Context
import com.example.airadvise.models.User
import com.google.gson.Gson

object SessionManager {
    private const val PREFS_NAME = "AirAdvisePrefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_REMEMBER_ME = "remember_me"

    // Save user's token once user login
    fun saveAuthToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveUserId(context: Context, userId: Long) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply()
    }

    // Save user data as JSON string
    fun saveUserData(context: Context, user: User) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER_DATA, userJson).apply()
    }

    // Get user data
    fun getUserData(context: Context): User? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                val gson = Gson()
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Set remember me flag
    fun setRememberMe(context: Context, rememberMe: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply()
    }

    // Get remember me flag
    fun getRememberMe(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    // Get token (for API request)
    fun getAuthToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    // Get user id
    fun getUserId(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(KEY_USER_ID, -1)
    }

    // For logout the user
    fun clearSession(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    // Check if the user is logged in
    fun isLoggedIn(context: Context): Boolean {
        return getAuthToken(context) != null
    }
}