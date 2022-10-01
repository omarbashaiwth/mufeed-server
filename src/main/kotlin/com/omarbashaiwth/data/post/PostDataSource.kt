package com.omarbashaiwth.data.post

import com.omarbashaiwth.utils.Constants

interface PostDataSource {

    suspend fun getAllPosts(
        page: Int,
        pageSize: Int
    ): List<Post>

    suspend fun getPostsByTag(tag: String): List<Post>

    suspend fun insertPost(post: Post): Boolean
}