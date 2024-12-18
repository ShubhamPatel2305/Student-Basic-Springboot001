package com.student.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "students")
data class Student (

    @Id val id: String? = null,
    val name:String,
    val email: String,
    val classTag: String,
    val age: Byte,
    val gender: Gender
)

enum class Gender{
    M,F,O
}