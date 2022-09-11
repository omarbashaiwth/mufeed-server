package com.omarbashaiwth

import com.omarbashaiwth.data.post.MongoPostDataSource
import com.omarbashaiwth.data.user.MongoUserDataSource
import com.omarbashaiwth.data.user.UserDataSource
import io.ktor.server.application.*
import com.omarbashaiwth.plugins.*
import com.omarbashaiwth.security.hash.SHA256Hashing
import com.omarbashaiwth.security.token.JwtTokenService
import com.omarbashaiwth.security.token.TokenConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongodbPassword = System.getenv("MONGODB_PASSWORD")
    val mongodbName = System.getenv("MONGODB_NAME")
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://omarbashaiwth:$mongodbPassword@cluster0.xdwjjxb.mongodb.net/$mongodbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(mongodbName)

    val tokenConfig = TokenConfig(
        secret = System.getenv("JWT_SECRET"),
        audience = environment.config.property("jwt.audience").getString(),
        issuer = environment.config.property("jwt.issuer").getString()
    )
    val userDataSource = MongoUserDataSource(db)
    val postDataSource = MongoPostDataSource(db)
    val hashingService = SHA256Hashing()
    val tokenService = JwtTokenService()

    configureSerialization()
    configureMonitoring()
    configureRouting(userDataSource,postDataSource,hashingService,tokenService,tokenConfig)
    configSecurity(tokenConfig)
}
