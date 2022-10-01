package com.omarbashaiwth.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val title: String,
    val shortDescription:String,
    val links: List<String>,
    val body:String,
    val tags: List<String>
)
