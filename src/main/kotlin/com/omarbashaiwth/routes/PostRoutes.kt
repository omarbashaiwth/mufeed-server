package com.omarbashaiwth.routes

import com.google.gson.Gson
import com.omarbashaiwth.data.post.Post
import com.omarbashaiwth.data.post.PostDataSource
import com.omarbashaiwth.data.requests.PostRequest
import com.omarbashaiwth.data.responses.PostResponse
import com.omarbashaiwth.utils.Constants
import com.omarbashaiwth.utils.Constants.DATE_PATTERN
import com.omarbashaiwth.utils.Constants.DEFAULT_PAGE_SIZE
import com.omarbashaiwth.utils.save
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*


private const val BASE_URL = "http://192.168.0.125:8080"

fun Route.createPost(
    postDataSource: PostDataSource
) {
    authenticate {
        post("posts/create") {
            val multiPart = call.receiveMultipart()
            var request: PostRequest? = null
            var fileName: String? = null

            multiPart.forEachPart { partData ->
                when (partData) {
                    is PartData.FormItem -> {
                        if (partData.name == Constants.FORM_ITEM_NAME) {
                            request = Gson().fromJson(
                                partData.value,
                                PostRequest::class.java
                            )
                        }
                    }
                    is PartData.FileItem -> {
                        fileName = partData.save("${Constants.POST_PICTURES_PATH}${Constants.POST_PICTURES_FOLDER__NAME}")
                    }
                    else -> Unit
                }
                partData.dispose
            }
            val postImageUrl = "$BASE_URL/${Constants.POST_PICTURES_FOLDER__NAME}$fileName"
            val currentDate = SimpleDateFormat(DATE_PATTERN,Locale.getDefault()).format(System.currentTimeMillis())!!
            request?.let {
                val successfullyCreatePost = postDataSource.insertPost(
                    Post(
                        title = it.title,
                        shortDescription = it.shortDescription,
                        links = it.links,
                        body = it.body,
                        imageUrl = postImageUrl,
                        date = currentDate,
                        tags = it.tags
                    )
                )
                if (successfullyCreatePost) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Conflict, "something went wrong")
                    return@post
                }
            }
        }
    }
}


fun Route.getAllPosts(
    postDataSource: PostDataSource
) {
    get("/posts/get") {
        val page = call.parameters[Constants.PARAM_PAGE]?.toInt() ?: 1
        val pageSize = call.parameters[Constants.PARAM_PAGE_SIZE]?.toInt() ?: DEFAULT_PAGE_SIZE
        val posts = postDataSource.getAllPosts(page, pageSize)
        val response = posts.map { post ->
            PostResponse(
                id = post.id,
                title = post.title,
                shortDescription = post.shortDescription,
                body = post.body,
                imageUrl = post.imageUrl,
                date = post.date,
                tags = post.tags,
                links = post.links
            )
        }
        call.respond(HttpStatusCode.OK, response)
    }
}

fun Route.getPostsByTag(
    postDataSource: PostDataSource
) {
    get("/posts/get-by-tag/") {
        val tag = call.parameters[Constants.PARAM_TAG]
        tag?.let {
            val result = postDataSource.getPostsByTag(it)
            val response = result.map { post ->
                PostResponse(
                    id = post.id,
                    title = post.title,
                    shortDescription = post.shortDescription,
                    body = post.body,
                    imageUrl = post.imageUrl,
                    date = post.date,
                    tags = post.tags,
                    links = post.links
                )
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}