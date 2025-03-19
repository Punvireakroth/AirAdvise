data class PaginatedResponse<T>(
    val data: List<T>,
    val current_page: Int,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)