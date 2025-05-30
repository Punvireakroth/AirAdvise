package com.example.airadvise.api


import com.example.airadvise.models.Activity
import com.example.airadvise.models.ActivityRecommendations
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.AirQualityForecast
import com.example.airadvise.models.Article
import com.example.airadvise.models.AuthResponse
import com.example.airadvise.models.City
import com.example.airadvise.models.Feedback
import com.example.airadvise.models.FeedbackResponse
import com.example.airadvise.models.HealthTip
import com.example.airadvise.models.Location
import com.example.airadvise.models.User
import com.example.airadvise.models.UserNotification
import com.example.airadvise.models.UserPreferences
import com.example.airadvise.models.request.ChangePasswordRequest
import com.example.airadvise.models.request.FavoriteCityRequest
import com.example.airadvise.models.request.FeedbackRequest
import com.example.airadvise.models.request.FeedbackResponseRequest
import com.example.airadvise.models.request.LoginRequest
import com.example.airadvise.models.request.RegisterRequest
//import com.example.airadvise.models.response.AirQualityResponse
import com.example.airadvise.models.response.AirQualityResponseData
import com.example.airadvise.models.response.ForecastResponse
import com.example.airadvise.models.response.LocationSearchResponse
import com.example.airadvise.models.response.MapAirQualityResponse
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
    // ARE IN OPERATE***
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

    // Air quality --------
    // IN OPERATE***
    @GET("api/air-quality/current")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<AirQualityResponseData>

    // Forecasts --------
    // IN OPERATE***
    @GET("api/locations/{id}/forecasts")
    suspend fun getForecasts(
        @Path("id") locationId: Long,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ForecastResponse>

    // Map and Location Management
    @GET("api/cities/search")
    suspend fun searchCities(@Query("query") query: String): Response<PaginatedResponse<City>>

    @GET("api/air-quality/map")
    suspend fun getMapAirQuality(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("zoom") zoom: Float,
        @Query("pollutant") pollutant: String
    ): Response<MapAirQualityResponse>

    @GET("api/air-quality/city/{cityId}")
    suspend fun getCityAirQuality(@Path("cityId") cityId: String): Response<AirQualityData>

    @GET("api/cities/{cityId}")
    suspend fun getCityDetails(@Path("cityId") cityId: String): Response<City>

    @POST("api/user/favorites")
    suspend fun addFavoriteCity(@Body request: FavoriteCityRequest): Response<Unit>

    @DELETE("api/user/favorites/{cityId}")
    suspend fun removeFavoriteCity(@Path("cityId") cityId: String): Response<Unit>


    // Notifications --------
    @GET("notifications")
    suspend fun getUserNotifications(): Response<List<UserNotification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") notificationId: Long): Response<MessageResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @GET("articles/{slug}")
    suspend fun getArticleBySlug(@Path("slug") slug: String): Response<Article>

    // Password management --------
    @POST("auth/password/reset")
    suspend fun requestPasswordReset(@Body email: Map<String, String>): Response<MessageResponse>

    @POST("auth/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
}