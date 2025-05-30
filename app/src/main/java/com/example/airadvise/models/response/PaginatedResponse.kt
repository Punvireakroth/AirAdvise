package com.example.airadvise.models.response

data class PaginatedResponse<T>(
    val current_page: Int,
    val data: List<T>,
    val first_page_url: String,
    val from: Int,
    val last_page: Int,
    val last_page_url: String,
    val links: List<PageLink>,
    val next_page_url: String?,
    val path: String,
    val per_page: Int,
    val prev_page_url: String?,
    val to: Int,
    val total: Int
)

data class PageLink(
    val url: String?,
    val label: String,
    val active: Boolean
)