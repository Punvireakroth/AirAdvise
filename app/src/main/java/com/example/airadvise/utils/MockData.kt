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
//import com.example.airadvise.models.response.AirQualityResponse
import com.example.airadvise.models.response.ErrorResponse
import com.example.airadvise.models.response.LocationSearchResponse
import com.example.airadvise.models.response.MessageResponse
import com.example.airadvise.models.response.PaginatedResponse

object MockData {
    fun getMockUser(): User {
        return User(
            id = 1,
            name = "Test User",
            email = "test@example.com",
            role = "user",
            createdAt = "2023-06-15T10:00:00Z",
            updatedAt = "2023-06-15T10:00:00Z"
        )
    }

    fun getMockUserPreferences(): UserPreferences {
        return UserPreferences(
            id = 1,
            userId = 1,
            notificationEnabled = true,
            aqiThreshold = 100,
            preferredLanguage = "en",
            temperatureUnit = "celsius",
            createdAt = "2023-06-15T10:00:00Z",
            updatedAt = "2023-06-15T10:00:00Z"
        )
    }

    fun getMockAirQualityData(): AirQualityData {
        return AirQualityData(
            id = 1,
            locationId = 1,
            aqi = 85,
            pm25 = 25.4,
            pm10 = 48.2,
            o3 = 35.1,
            no2 = 15.2,
            so2 = 5.1,
            co = 0.8,
            category = "Moderate",
            source = "Test API",
            timestamp = "2023-06-15T10:30:00Z",
            createdAt = "2023-06-15T10:00:00Z",
            updatedAt = "2023-06-15T10:00:00Z"
        )
    }

    fun getMockLocation(): Location {
        return Location(
            id = 1,
            cityName = "Phnom Penh",
            stateProvince = null,
            country = "Cambodia",
            countryCode = "KH",
            latitude = 11.5564,
            longitude = 104.9282,
            timezone = "Asia/Phnom_Penh",
            isActive = true,
            isFavorite = true,
            createdAt = "2023-06-15T10:00:00Z",
            updatedAt = "2023-06-15T10:00:00Z"
        )
    }

    fun getMockLocations(): List<Location> {
        return listOf(
            getMockLocation(),
            Location(
                id = 2,
                cityName = "Siem Reap",
                stateProvince = null,
                country = "Cambodia",
                countryCode = "KH",
                latitude = 13.3633,
                longitude = 103.8566,
                timezone = "Asia/Phnom_Penh",
                isActive = true,
                isFavorite = false,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Location(
                id = 3,
                cityName = "Battambang",
                stateProvince = null,
                country = "Cambodia",
                countryCode = "KH",
                latitude = 13.1023,
                longitude = 103.1994,
                timezone = "Asia/Phnom_Penh",
                isActive = true,
                isFavorite = false,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            )
        )
    }

    fun getMockHealthTips(): List<HealthTip> {
        return listOf(
            HealthTip(
                id = 1,
                title = "Stay Hydrated",
                content = "During moderate air quality conditions, drinking plenty of water helps your body flush out toxins and maintain respiratory health.",
                minAqi = 51,
                maxAqi = 100,
                createdBy = 1,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            HealthTip(
                id = 2,
                title = "Limit Outdoor Activities",
                content = "When air quality is unhealthy, consider limiting prolonged outdoor activities, especially if you have respiratory issues.",
                minAqi = 101,
                maxAqi = 150,
                createdBy = 1,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            HealthTip(
                id = 3,
                title = "Use Air Purifier",
                content = "Using an air purifier with HEPA filter can significantly improve indoor air quality during high pollution days.",
                minAqi = 101,
                maxAqi = 200,
                createdBy = 1,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            )
        )
    }

    fun getMockActivities(): List<Activity> {
        return listOf(
            Activity(
                id = 1,
                name = "Running",
                description = "Outdoor running or jogging",
                maxSafeAqi = 100,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Activity(
                id = 2,
                name = "Cycling",
                description = "Outdoor biking or cycling",
                maxSafeAqi = 100,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Activity(
                id = 3,
                name = "Walking",
                description = "Casual walking outdoors",
                maxSafeAqi = 150,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Activity(
                id = 4,
                name = "Outdoor Dining",
                description = "Eating at outdoor restaurants or cafes",
                maxSafeAqi = 150,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Activity(
                id = 5,
                name = "Picnic",
                description = "Having a picnic in park or outdoor space",
                maxSafeAqi = 100,
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            )
        )
    }

    fun getMockActivityRecommendations(): ActivityRecommendations {
        val allActivities = getMockActivities()
        val aqi = 85

        return ActivityRecommendations(
            safeActivities = allActivities.filter { it.maxSafeAqi >= aqi },
            unsafeActivities = allActivities.filter { it.maxSafeAqi < aqi }
        )
    }

    fun getMockFeedback(): List<Feedback> {
        return listOf(
            Feedback(
                id = 1,
                userId = 1,
                subject = "App Suggestion",
                message = "I think it would be great to add notifications for sudden AQI changes.",
                status = "submitted",
                createdAt = "2023-06-15T10:00:00Z",
                updatedAt = "2023-06-15T10:00:00Z"
            ),
            Feedback(
                id = 2,
                userId = 1,
                subject = "Data Accuracy Issue",
                message = "The AQI for Phnom Penh seems different from what other sources report.",
                status = "in_review",
                createdAt = "2023-06-14T15:30:00Z",
                updatedAt = "2023-06-15T09:45:00Z"
            ),
            Feedback(
                id = 3,
                userId = 1,
                subject = "Feature Request",
                message = "Please add historical data graphs for AQI trends.",
                status = "resolved",
                createdAt = "2023-06-10T12:00:00Z",
                updatedAt = "2023-06-12T14:20:00Z"
            )
        )
    }

    fun getMockFeedbackResponse(): FeedbackResponse {
        return FeedbackResponse(
            id = 1,
            feedbackId = 3,
            adminId = 2,
            response = "Thank you for your suggestion! We plan to add historical data graphs in our next update.",
            createdAt = "2023-06-12T14:20:00Z",
            updatedAt = "2023-06-12T14:20:00Z"
        )
    }

    fun getMockUserNotifications(): List<UserNotification> {
        return listOf(
            UserNotification(
                id = 1,
                userId = 1,
                title = "High AQI Alert",
                message = "AQI in Phnom Penh has reached 150 (Unhealthy for Sensitive Groups)",
                locationId = 1,
                aqiValue = 150,
                isRead = false,
                createdAt = "2023-06-15T09:00:00Z",
                updatedAt = "2023-06-15T09:00:00Z"
            ),
            UserNotification(
                id = 2,
                userId = 1,
                title = "Forecast Update",
                message = "Tomorrow's AQI is predicted to improve to Moderate levels.",
                locationId = 1,
                aqiValue = 75,
                isRead = true,
                createdAt = "2023-06-14T10:30:00Z",
                updatedAt = "2023-06-14T11:15:00Z"
            ),
            UserNotification(
                id = 3,
                userId = 1,
                title = "New Health Tip",
                message = "Check out our new health recommendations for moderate AQI levels.",
                locationId = null,
                aqiValue = null,
                isRead = false,
                createdAt = "2023-06-13T15:45:00Z",
                updatedAt = "2023-06-13T15:45:00Z"
            )
        )
    }

    fun getMockArticles(): List<Article> {
        return listOf(
            Article(
                id = 1,
                title = "Understanding AQI Values",
                slug = "understanding-aqi-values",
                content = "The Air Quality Index (AQI) is a numerical scale used to communicate how polluted the air is and what associated health effects might be...",
                featuredImage = "https://example.com/images/aqi_explanation.jpg",
                summary = "Learn about AQI values and what they mean for your health",
                createdBy = 2,
                isPublished = true,
                publishedAt = "2023-06-10T08:00:00Z",
                category = "Education",
                viewCount = 245,
                createdAt = "2023-06-09T14:30:00Z",
                updatedAt = "2023-06-10T08:00:00Z"
            ),
            Article(
                id = 2,
                title = "Protecting Children from Air Pollution",
                slug = "protecting-children-from-air-pollution",
                content = "Children are particularly vulnerable to air pollution because their lungs are still developing and they breathe more air per pound of body weight than adults...",
                featuredImage = "https://example.com/images/children_protection.jpg",
                summary = "Tips to keep children safe during high pollution days",
                createdBy = 2,
                isPublished = true,
                publishedAt = "2023-06-12T10:15:00Z",
                category = "Health",
                viewCount = 187,
                createdAt = "2023-06-11T16:45:00Z",
                updatedAt = "2023-06-12T10:15:00Z"
            ),
            Article(
                id = 3,
                title = "Indoor Plants That Improve Air Quality",
                slug = "indoor-plants-improve-air-quality",
                content = "Certain indoor plants can help filter common air pollutants, making them a beautiful and functional addition to your home...",
                featuredImage = "https://example.com/images/air_purifying_plants.jpg",
                summary = "Discover which plants can help clean your indoor air",
                createdBy = 2,
                isPublished = true,
                publishedAt = "2023-06-14T09:30:00Z",
                category = "Lifestyle",
                viewCount = 312,
                createdAt = "2023-06-13T15:20:00Z",
                updatedAt = "2023-06-14T09:30:00Z"
            )
        )
    }

    fun getMockAuthResponse(): AuthResponse {
        return AuthResponse(
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwMDAvYXBpL2F1dGgvbG9naW4iLCJpYXQiOjE2ODY4NDE4MDAsImV4cCI6MTY4Njg0NTQwMCwibmJmIjoxNjg2ODQxODAwLCJqdGkiOiJNWGRaODJ3dXBlVnF4c1lwIiwic3ViIjoiMSIsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjcifQ.e2DQjxD5xo84PvPr5qBUfTkUfEZk_LjhLe6RXm1FI2A",
            user = getMockUser()
        )
    }

//    fun getMockAirQualityResponse(): AirQualityResponse {
//        return AirQualityResponse(
//            airQuality = getMockAirQualityData(),
//            location = getMockLocation(),
//            healthTips = getMockHealthTips().filter {
//                val aqi = getMockAirQualityData().aqi
//                it.minAqi <= aqi && it.maxAqi >= aqi
//            },
//            safeActivities = getMockActivities().filter {
//                it.maxSafeAqi >= getMockAirQualityData().aqi
//            },
//            unsafeActivities = getMockActivities().filter {
//                it.maxSafeAqi < getMockAirQualityData().aqi
//            }
//        )
//    }

    fun getMockLocationSearchResponse(): LocationSearchResponse {
        return LocationSearchResponse(
            locations = getMockLocations()
        )
    }

    fun getMockPaginatedArticlesResponse(): PaginatedResponse<Article> {
        return PaginatedResponse(
            data = getMockArticles(),
            current_page = 1,
            last_page = 1,
            per_page = 10,
            total = 3
        )
    }

    fun getMockMessageResponse(message: String = "Operation completed successfully"): MessageResponse {
        return MessageResponse(message)
    }

    fun getMockErrorResponse(message: String = "An error occurred"): ErrorResponse {
        return ErrorResponse(
            message = message,
            errors = mapOf(
                "email" to listOf("The email has already been taken."),
                "password" to listOf("The password must be at least 8 characters.")
            )
        )
    }
}