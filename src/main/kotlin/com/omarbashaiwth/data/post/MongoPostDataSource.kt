package com.omarbashaiwth.data.post

import org.litote.kmongo.MongoOperator
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoPostDataSource(
    db: CoroutineDatabase
): PostDataSource {

    private val posts = db.getCollection<Post>()

    override suspend fun getAllPosts(
        page: Int,
        limit: Int,
    ): List<Post> {
        return posts.find()
            .skip((page - 1)*limit)
            .limit(limit)
            .partial(true)
            .descendingSort(Post::date)
            .toList()
    }

    override suspend fun getPostsByTag(tag: String): List<Post> {
        return posts.find(Post::tags contains (tag)).toList()
    }

    override suspend fun insertPost(post: Post): Boolean {
        return posts.insertOne(post).wasAcknowledged()
    }
}