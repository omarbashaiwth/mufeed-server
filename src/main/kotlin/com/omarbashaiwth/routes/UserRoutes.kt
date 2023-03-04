package com.omarbashaiwth.routes

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.fromStream
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.EmailIdentifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import com.omarbashaiwth.data.requests.AuthRequest
import com.omarbashaiwth.data.responses.AuthResponse
import com.omarbashaiwth.data.responses.SimpleResponse
import com.omarbashaiwth.data.user.User
import com.omarbashaiwth.data.user.UserDataSource
import com.omarbashaiwth.security.hash.HashingService
import com.omarbashaiwth.security.hash.SaltedHash
import com.omarbashaiwth.security.token.TokenClaim
import com.omarbashaiwth.security.token.TokenConfig
import com.omarbashaiwth.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.CompletableDeferred
import java.io.InputStream

fun Route.signup(
    userDataSource: UserDataSource,
    hashingService: HashingService
){
    post("user/signup") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val usernameUnique = userDataSource.getUserByUsername(request.username) == null
        if (usernameUnique) {
            val saltedHashPassword = hashingService.generateSaltedHash(request.password)
            val user = User(
                username = request.username,
                password = saltedHashPassword.hash,
                salt = saltedHashPassword.salt
            )
            val successfullyInsertUser = userDataSource.insertUser(user)
            if (successfullyInsertUser) {
                call.respond(HttpStatusCode.OK, SimpleResponse<Unit>(true,"Successfully created user"))
            } else {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse<Unit>(false,"Something went wrong"))
            }
        } else {
            call.respond(HttpStatusCode.OK,SimpleResponse<Unit>(false,"Username already exit"))
        }
    }
}

fun Route.login(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("user/login") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.OK,SimpleResponse<Unit>(false,"Username incorrect"))

        } else {
            val passwordMatching = hashingService.verify(
                input = request.password,
                saltedHash = SaltedHash(
                    hash = user.password,
                    salt = user.salt
                )
            )
            if (!passwordMatching) {
                call.respond(HttpStatusCode.OK,SimpleResponse<Unit>(false,"Password incorrect"))
                return@post
            }

            val token = tokenService.generate(
                tokenConfig = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = user.id.toString()
                ),
                TokenClaim(
                    "email",
                    user.username
                )
            )
            call.respond(HttpStatusCode.OK,SimpleResponse(true,"Successfully logged in",AuthResponse(token)))
            registerUserInFirebase(request.username,request.password)

        }

    }
}

private suspend fun PipelineContext<Unit,ApplicationCall>.registerUserInFirebase(email: String, password:String){
    try {
        val userRecord = FirebaseAuth.getInstance().createUser(
            UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
        )
        call.respondText("User created successfully: ${userRecord.uid}")
    } catch (e: Exception) {
        call.respondText("Error creating user: ${e.message}")
    }

}