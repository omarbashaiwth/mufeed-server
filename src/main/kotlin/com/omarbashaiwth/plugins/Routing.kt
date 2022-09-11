package com.omarbashaiwth.plugins

import com.omarbashaiwth.data.post.PostDataSource
import com.omarbashaiwth.data.user.UserDataSource
import com.omarbashaiwth.routes.*
import com.omarbashaiwth.security.hash.HashingService
import com.omarbashaiwth.security.token.TokenConfig
import com.omarbashaiwth.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    postDataSource: PostDataSource,
    hashingService: HashingService,
    tokeService: TokenService,
    tokenConfig: TokenConfig
) {

    routing {
        signup(userDataSource, hashingService)
        login(userDataSource,hashingService,tokeService,tokenConfig)

        createPost(postDataSource)
        getAllPosts(postDataSource)
        getPostsByTag(postDataSource)

        static {
            resources("static")
        }
    }
}
