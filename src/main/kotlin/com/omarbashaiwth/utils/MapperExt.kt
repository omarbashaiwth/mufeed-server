package com.omarbashaiwth.utils

import com.omarbashaiwth.data.post.Post
import com.omarbashaiwth.data.responses.PostResponse

fun Post.toResponse(): PostResponse {
    return PostResponse(
        id = id,
        title = title,
        shortDescription = shortDescription,
        links = links,
        body = body,
        imageUrl = imageUrl,
        date = date,
        tags = tags,
    )
}

fun List<Post>.toResponseList(): List<PostResponse> {
    return this.map { post ->
        PostResponse(
            id = post.id,
            title = post.title,
            shortDescription = post.shortDescription,
            body = post.body,
            imageUrl = post.imageUrl,
            date = post.date,
            tags = post.tags,
            links = post.links,
        )
    }
}