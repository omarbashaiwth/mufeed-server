package com.omarbashaiwth.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val id: String,
    val title: String,
    val shortDescription: String,
    val links: List<String>,
    val body:String,
    val imageUrl: String,
    val tags: List<String>
)
