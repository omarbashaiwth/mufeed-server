package com.omarbashaiwth.plugins

import com.omarbashaiwth.fcm.FcmTokenDataSource
import com.omarbashaiwth.data.post.PostDataSource
import com.omarbashaiwth.data.user.UserDataSource
import com.omarbashaiwth.routes.*
import com.omarbashaiwth.security.hash.HashingService
import com.omarbashaiwth.security.token.TokenConfig
import com.omarbashaiwth.security.token.TokenService
import io.ktor.client.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    postDataSource: PostDataSource,
    fcmTokenDataSource: FcmTokenDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    httpClient: HttpClient,
    fcmServerKey: String,
) {

    routing {
        signup(userDataSource, hashingService)
        login(userDataSource,hashingService,tokenService,tokenConfig)

        createPost(
            postDataSource = postDataSource,
            fcmTokenDataSource = fcmTokenDataSource,
            httpClient = httpClient,
            fcmServerKey = fcmServerKey,
        )
        saveFcmToken(fcmTokenDataSource)
        getAllPosts(postDataSource)
        getPostsByTag(postDataSource)

        static {
            resources("static")
        }
    }
}
