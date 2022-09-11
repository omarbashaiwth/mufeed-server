package com.omarbashaiwth.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val title: String,
    val body:String,
    val imageUrl: String,
    val tags: List<String>

)
