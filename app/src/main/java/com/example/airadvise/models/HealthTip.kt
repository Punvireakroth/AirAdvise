data class HealthTip(
    val id: Long,
    val title: String,
    val content: String,
    val minAqi: Int,
    val maxAqi: Int,
    val createdBy: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null
)