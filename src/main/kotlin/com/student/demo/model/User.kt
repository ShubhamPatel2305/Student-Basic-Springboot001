package com.student.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collation = "users")
data class User (
    @Id val id:String = UUID.randomUUID().toString(),
    val email:String,
    val name:String,
    val passwordHash:String,
    val roles: List<String> = listOf("USER"),
    val refreshToken:String? = null
)

data class UserReg(
    val email:String,
    val name:String,
    val password:String,
    val roles: List<String>
)

data class UserLogin(
    val email:String,
    val password: String
)

data class UserLoginRes(
    val email:String,
    val name:String,
    val accessToken:String,
    val refreshToken:String
)