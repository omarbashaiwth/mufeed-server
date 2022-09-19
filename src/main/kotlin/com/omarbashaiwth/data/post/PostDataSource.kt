package com.omarbashaiwth.data.post

import com.omarbashaiwth.utils.Constants

interface PostDataSource {

    suspend fun getAllPosts(
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<Post>

    suspend fun getPostsByTag(tag: String): List<Post>

    suspend fun insertPost(post: Post): Boolean
}