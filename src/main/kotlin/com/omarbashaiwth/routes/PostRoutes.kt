package com.omarbashaiwth.routes

import com.google.gson.Gson
import com.omarbashaiwth.fcm.FcmTokenDataSource
import com.omarbashaiwth.data.post.Post
import com.omarbashaiwth.data.post.PostDataSource
import com.omarbashaiwth.data.requests.PostRequest
import com.omarbashaiwth.utils.Constants
import com.omarbashaiwth.utils.Constants.DATE_PATTERN
import com.omarbashaiwth.utils.Constants.DEFAULT_PAGE_SIZE
import com.omarbashaiwth.utils.save
import com.omarbashaiwth.utils.toResponseList
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.List


private const val BASE_URL = "http://192.168.100.14:8080"

fun Route.createPost(
    postDataSource: PostDataSource,
    fcmTokenDataSource: FcmTokenDataSource,
    httpClient: HttpClient,
    fcmServerKey: String,
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
            val currentDate = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(System.currentTimeMillis())!!
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
                    val tokens = fcmTokenDataSource.getAllTokens().map { it.token }
                    sendPushNotification(
                        httpClient = httpClient,
                        fcmServerKey = fcmServerKey,
                        fcmTokens = tokens,
                        title = it.title,
                        shortDescription = it.shortDescription,
                        postImageUrl = postImageUrl
                    )
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
        val response = posts.toResponseList()
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
            val response = result.toResponseList()
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

@OptIn(InternalAPI::class)
private suspend fun sendPushNotification(
    httpClient: HttpClient,
    fcmServerKey: String,
    fcmTokens: List<String>,
    title: String,
    shortDescription: String,
    postImageUrl: String
) = withContext(Dispatchers.IO) {
    val regIds = "[\"${fcmTokens.joinToString("\",\"")}\"]"
    println("tokens = $regIds")
    try {
        httpClient.post {
            url("https://fcm.googleapis.com/fcm/send")
            header("Authorization", "key=$fcmServerKey")
            header("Content-Type", "application/json")
            contentType(ContentType.Application.Json)

            body = """
            {
                "registration_ids":$regIds,
                "notification":{
                    "title":"$title",
                    "body":"$shortDescription",
                    "icon":"$postImageUrl"
                }
            }
            """
        }
    } catch (e:Exception){
        print("ERROR: ${e.localizedMessage}")
    }
}