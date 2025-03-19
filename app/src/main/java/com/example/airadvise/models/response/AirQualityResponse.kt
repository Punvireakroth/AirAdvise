import com.example.airadvise.models.AirQualityData

data class AirQualityResponse(
    val airQuality: AirQualityData,
    val location: Location,
    val healthTips: List<HealthTip>,
    val safeActivities: List<Activity>,
    val unsafeActivities: List<Activity>
)