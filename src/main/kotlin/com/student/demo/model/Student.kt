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

data class ListStudentsInput(
    val name: String? = null,
    val age: Int? = null,
    val classTag: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val page:Int,
    val pageSize:Int
)

enum class Gender{
    M,F,O
}