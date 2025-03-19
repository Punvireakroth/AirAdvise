data class Location(
    val id: Long,
    val cityName: String,
    val stateProvince: String?,
    val country: String,
    val countryCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val isActive: Boolean = true,
    val isFavorite: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)