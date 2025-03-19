data class FeedbackRequest(
    val subject: String,
    val message: String
)

data class FeedbackResponseRequest(
    val response: String
)