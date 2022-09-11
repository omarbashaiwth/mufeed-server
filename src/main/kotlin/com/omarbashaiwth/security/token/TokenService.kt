package com.omarbashaiwth.security.token

interface TokenService {
    fun generate(
        tokenConfig: TokenConfig,
        vararg claims: TokenClaim
    ): String
}