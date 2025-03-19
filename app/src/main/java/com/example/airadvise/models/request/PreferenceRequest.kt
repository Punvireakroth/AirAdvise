data class UpdatePreferencesRequest(
    val notification_enabled: Boolean,
    val aqi_threshold: Int,
    val preferred_language: String,
    val temperature_unit: String
)