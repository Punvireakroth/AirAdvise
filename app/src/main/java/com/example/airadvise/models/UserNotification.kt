data class UserNotification(
    val id: Long,
    val userId: Long,
    val title: String,
    val message: String,
    val locationId: Long?,
    val aqiValue: Int?,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)