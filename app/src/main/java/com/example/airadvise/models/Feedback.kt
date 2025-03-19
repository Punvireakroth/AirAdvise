data class Feedback(
    val id: Long,
    val userId: Long,
    val subject: String,
    val message: String,
    val status: String, // "submitted", "in_review", "resolved"
    val createdAt: String? = null,
    val updatedAt: String? = null
)