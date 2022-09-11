package com.omarbashaiwth.security.token

data class TokenConfig(
    val secret: String,
    val audience:String,
    val issuer: String
)
