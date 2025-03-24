package com.example.airadvise.utils

import retrofit2.Response

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Response body is empty")
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error occurred"
            Resource.Error(errorMsg)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Error occurred")
    }
}
