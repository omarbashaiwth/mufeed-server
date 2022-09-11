package com.omarbashaiwth.security.hash

import io.ktor.util.*
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256Hashing: HashingService {
    override fun generateSaltedHash(input: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$input")
        return SaltedHash(
            hash = hash,
            salt = saltAsHex
        )
    }

    override fun verify(input: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex("${saltedHash.salt}$input") == saltedHash.hash
    }
}