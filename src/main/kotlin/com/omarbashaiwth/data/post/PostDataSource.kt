package com.omarbashaiwth.data.post

interface PostDataSource {

    suspend fun getAllPosts(page: Int, limit: Int): List<Post>

    suspend fun getPostsByTag(tag: String): List<Post>

    suspend fun insertPost(post: Post): Boolean
}