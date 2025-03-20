package com.example.airadvise.models
data class Article(
    val id: Long,
    val title: String,
    val slug: String,
    val content: String,
    val featuredImage: String?,
    val summary: String?,
    val createdBy: Long,
    val isPublished: Boolean,
    val publishedAt: String?,
    val category: String?,
    val viewCount: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)