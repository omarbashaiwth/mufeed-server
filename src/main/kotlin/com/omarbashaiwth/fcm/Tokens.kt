package com.omarbashaiwth.fcm

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Tokens(
    val token: String,
    @BsonId val id: String = ObjectId().toString()
)
