package com.omarbashaiwth.routes

import com.google.firebase.cloud.StorageClient
import com.google.gson.Gson
import com.omarbashaiwth.data.post.Post
import com.omarbashaiwth.data.post.PostDataSource
import com.omarbashaiwth.data.requests.PostRequest
import com.omarbashaiwth.fcm.FcmTokenDataSource
import com.omarbashaiwth.plugins.email
import com.omarbashaiwth.utils.Constants
import com.omarbashaiwth.utils.Constants.DATE_PATTERN
import com.omarbashaiwth.utils.Constants.DEFAULT_PAGE_SIZE
import com.omarbashaiwth.utils.generateImagePath
import com.omarbashaiwth.utils.toResponseList
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


private const val BASE_URL = "http://192.168.0.125:8080"

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
            val email = call.principal<JWTPrincipal>()?.email ?: ""
            var imageUrl: String? = null

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
                        if (partData.contentType?.match(ContentType.Image.Any) == true) {
                            imageUrl = partData.generateImagePath(email)
                            partData.uploadImageToFirebase(
                                call = call,
                                imagePath = imageUrl ?: ""
                            )
                        } else {
                            throw UnsupportedMediaTypeException(ContentType.Image.Any)
                        }
                    }
                    else -> Unit
                }
                partData.dispose
            }
            val currentDate = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(System.currentTimeMillis())!!
            if (imageUrl != null) {
                request?.let {
                    val successfullyCreatePost = postDataSource.insertPost(
                        Post(
                            title = it.title,
                            shortDescription = it.shortDescription,
                            links = it.links,
                            body = it.body,
                            imageUrl = imageUrl ?: "",
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
                            postImageUrl = imageUrl ?: ""
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
    } catch (e: Exception) {
        print("ERROR: ${e.localizedMessage}")
    }
}

suspend fun PartData.FileItem.uploadImageToFirebase(
    call: ApplicationCall,
    imagePath: String
){
    val inputStream = withContext(Dispatchers.IO) {
        streamProvider().readBytes()
    }
    val blob = StorageClient.getInstance().bucket()
        .create(imagePath, inputStream)
    val downloadUrl = blob.signUrl(1 , TimeUnit.HOURS).toString()
    call.respondText(downloadUrl)

}