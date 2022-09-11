package com.omarbashaiwth.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

class JwtTokenService: TokenService {
    override fun generate(tokenConfig: TokenConfig, vararg claims: TokenClaim): String {
        var token = JWT.create()
            .withIssuer(tokenConfig.issuer)
            .withAudience(tokenConfig.audience)
        claims.forEach {
            token = token.withClaim(it.name,it.value)
        }
        return token.sign(Algorithm.HMAC256(tokenConfig.secret))
    }
}