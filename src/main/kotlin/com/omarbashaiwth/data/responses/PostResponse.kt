package com.omarbashaiwth.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val title: String,
    val body:String,
    val imageUrl: String,
    val tags: List<String>
)
