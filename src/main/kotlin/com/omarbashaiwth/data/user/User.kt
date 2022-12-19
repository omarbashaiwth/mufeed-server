package com.omarbashaiwth.data.user

import com.omarbashaiwth.security.hash.SaltedHash
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val username: String,
    val password: String,
    val salt: String,
    @BsonId val id: String = ObjectId().toString()
)
