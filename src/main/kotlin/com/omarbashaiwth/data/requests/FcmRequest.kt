package com.omarbashaiwth.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class FcmRequest(
    val token: String
)
