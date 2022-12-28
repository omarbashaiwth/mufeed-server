package com.omarbashaiwth.routes

import com.omarbashaiwth.fcm.Tokens
import com.omarbashaiwth.fcm.FcmTokenDataSource
import com.omarbashaiwth.data.requests.FcmRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.saveFcmToken(
    tokenDataSource: FcmTokenDataSource
) {
    post("tokens/save") {
        val token = call.receiveNullable<FcmRequest>()?.token
        if (token != null) {
            val tokenSaveAcknowledge = tokenDataSource.saveToken(Tokens(token))
            if (tokenSaveAcknowledge) {
                call.respond(HttpStatusCode.OK, "Successfully token saved")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Token not saved, ${HttpStatusCode.BadRequest.description}")
            }
        }
    }
}

