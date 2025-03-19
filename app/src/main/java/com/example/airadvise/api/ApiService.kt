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
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
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
    @GET("air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<AirQualityData>

    @GET("locations/{id}/air-quality")
    suspend fun getAirQualityByLocation(@Path("id") locationId: Long): Response<AirQualityData>

    // Forecasts --------
    @GET("locations/{id}/forecasts")
    suspend fun getForecasts(@Path("id") locationId: Long): Response<List<AirQualityForecast>>

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