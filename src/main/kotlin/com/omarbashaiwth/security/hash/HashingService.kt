package com.omarbashaiwth.security.hash

interface HashingService {

    fun generateSaltedHash(input: String, saltLength: Int = 32): SaltedHash
    fun verify(input: String, saltedHash: SaltedHash): Boolean
}