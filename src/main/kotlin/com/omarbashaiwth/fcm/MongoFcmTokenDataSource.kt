package com.omarbashaiwth.fcm

import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoFcmTokenDataSource(
    db: CoroutineDatabase
): FcmTokenDataSource {
    private val tokens = db.getCollection<Tokens>()
    override suspend fun saveToken(token: Tokens): Boolean {
        return tokens.insertOne(token).wasAcknowledged()
    }

    override suspend fun getAllTokens(): List<Tokens> {
        return tokens.find().toList()
    }
}