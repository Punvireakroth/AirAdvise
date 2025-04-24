package com.example.airadvise.api


import com.example.airadvise.models.Activity
import com.example.airadvise.models.ActivityRecommendations
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.AirQualityForecast
import com.example.airadvise.models.Article
import com.example.airadvise.models.AuthResponse
import com.example.airadvise.models.Feedback
import com.example.airadvise.models.FeedbackResponse
import com.example.airadvise.models.HealthTip
import com.example.airadvise.models.Location
import com.example.airadvise.models.User
import com.example.airadvise.models.UserNotification
import com.example.airadvise.models.UserPreferences
import com.example.airadvise.models.request.ChangePasswordRequest
import com.example.airadvise.models.request.FeedbackRequest
import com.example.airadvise.models.request.FeedbackResponseRequest
import com.example.airadvise.models.request.LoginRequest
import com.example.airadvise.models.request.RegisterRequest
//import com.example.airadvise.models.response.AirQualityResponse
import com.example.airadvise.models.response.AirQualityResponseData
import com.example.airadvise.models.response.ForecastResponse
import com.example.airadvise.models.response.LocationSearchResponse
import com.example.airadvise.models.response.MessageResponse
import com.example.airadvise.models.response.PaginatedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth endpoints ---------
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/logout")
    suspend fun logout(): Response<MessageResponse>

    // User endpoints --------
    @GET("user")
    suspend fun getCurrentUser(): Response<User>

    @PUT("user")
    suspend fun updateUser(@Body user: User): Response<User>

    // User Preferences --------
    @GET("user/preferences")
    suspend fun getUserPreferences(): Response<UserPreferences>

    @PUT("user/preferences")
    suspend fun updateUserPreferences(@Body preferences: UserPreferences): Response<UserPreferences>

    // Locations --------
    @GET("locations")
    suspend fun getLocations(): Response<List<Location>>

    @GET("locations/{id}")
    suspend fun getLocation(@Path("id") id: Long): Response<Location>

    @POST("locations/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: Long): Response<MessageResponse>

    // Search functionality
    @GET("locations/search")
    suspend fun searchLocations(@Query("query") query: String): Response<LocationSearchResponse>

    // Air quality --------

    @GET("api/air-quality/current")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<AirQualityResponseData>

    @GET("locations/{id}/air-quality")
    suspend fun getAirQualityByLocation(@Path("id") locationId: Long): Response<AirQualityData>

    // Forecasts --------
    @GET("api/locations/{id}/forecasts")
    suspend fun getForecasts(@Path("id") locationId: Long): Response<ForecastResponse>

    @GET("api/forecasts/by-location")
    suspend fun getForecastsByLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ForecastResponse>

    // Health tips --------
    @GET("health-tips")
    suspend fun getHealthTips(@Query("aqi") aqi: Int? = null): Response<List<HealthTip>>

    // Activities --------
    @GET("activities")
    suspend fun getActivities(): Response<List<Activity>>


    // Feedback endpoints --------
    @GET("feedback")
    suspend fun getFeedback(): Response<List<Feedback>>

    @POST("feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): Response<Feedback>

    @GET("feedback/{id}")
    suspend fun getFeedbackDetails(@Path("id") id: Long): Response<Feedback>

    // Feedback responses (for admin) --------
    @POST("feedback/{id}/respond")
    suspend fun respondToFeedback(
        @Path("id") feedbackId: Long,
        @Body request: FeedbackResponseRequest
    ): Response<FeedbackResponse>

    // Notifications --------
    @GET("notifications")
    suspend fun getUserNotifications(): Response<List<UserNotification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") notificationId: Long): Response<MessageResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    // Articles & Educational Content --------
    @GET("articles")
    suspend fun getArticles(
        @Query("category") category: String? = null,
        @Query("page") page: Int? = null
    ): Response<PaginatedResponse<Article>>

    @GET("articles/{slug}")
    suspend fun getArticleBySlug(@Path("slug") slug: String): Response<Article>

    // Activity recommendations --------
    @GET("activities/recommendations")
    suspend fun getActivityRecommendations(
        @Query("aqi") aqi: Int,
        @Query("location_id") locationId: Long? = null
    ): Response<ActivityRecommendations>

    // Password management --------
    @POST("auth/password/reset")
    suspend fun requestPasswordReset(@Body email: Map<String, String>): Response<MessageResponse>

    @POST("auth/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    // Air quality history --------
    @GET("locations/{id}/air-quality/history")
    suspend fun getAirQualityHistory(
        @Path("id") locationId: Long,
        @Query("days") days: Int = 7
    ): Response<List<AirQualityData>>

    // User location management --------
    @GET("user/locations")
    suspend fun getUserLocations(): Response<List<Location>>

    @POST("user/locations/{id}")
    suspend fun saveUserLocation(@Path("id") locationId: Long): Response<MessageResponse>

    @DELETE("user/locations/{id}")
    suspend fun removeUserLocation(@Path("id") locationId: Long): Response<MessageResponse>

    // Best day recommendation for activities --------
    @GET("forecasts/best-day")
    suspend fun getBestDayForActivity(
        @Query("activity_id") activityId: Long,
        @Query("location_id") locationId: Long
    ): Response<AirQualityForecast>
}