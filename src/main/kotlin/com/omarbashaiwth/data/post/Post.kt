package com.omarbashaiwth.data.post

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Post(
    val title: String,
    val body: String,
    val imageUrl:String,
    val date: Long,
    val tags: List<String>,
    @BsonId val id: ObjectId = ObjectId()
)

