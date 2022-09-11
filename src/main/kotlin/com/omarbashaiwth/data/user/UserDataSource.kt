package com.omarbashaiwth.data.user

interface UserDataSource {

    suspend fun insertUser(user: User): Boolean
    suspend fun getUserByUsername(username: String): User?

}