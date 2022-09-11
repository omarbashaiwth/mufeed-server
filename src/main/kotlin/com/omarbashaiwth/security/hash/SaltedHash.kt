package com.omarbashaiwth.security.hash

data class SaltedHash(
    val hash: String,
    val salt: String
)
