package com.omarbashaiwth.fcm

import com.omarbashaiwth.fcm.Tokens

interface FcmTokenDataSource {

    suspend fun saveToken(token: Tokens): Boolean

    suspend fun getAllTokens(): List<Tokens>
}