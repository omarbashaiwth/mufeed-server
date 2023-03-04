package com.omarbashaiwth.utils

import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.content.*
import java.io.File
import java.util.*

fun PartData.FileItem.save(path: String): String {
    val fileBytes = streamProvider().readBytes()
    val fileExtension = originalFileName?.takeLastWhile { it != '.' }
    val fileName = "${UUID.randomUUID()}.$fileExtension"
    val folder = File(path)
    folder.mkdirs()
    File("$path$fileName").writeBytes(fileBytes)
    return fileName
}

fun PartData.FileItem.generateImagePath(email: String): String {
    val userId = FirebaseAuth.getInstance().getUserByEmail(email).uid
    val imageExtension = originalFileName?.takeLastWhile { it != '.' } ?: "jpg"
    val randomName = UUID.randomUUID().toString()
    return "images/$userId/$randomName.$imageExtension"
}