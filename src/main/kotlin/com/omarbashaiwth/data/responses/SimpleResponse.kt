package com.omarbashaiwth.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
