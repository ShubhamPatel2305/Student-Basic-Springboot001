package com.student.demo.repository

import com.student.demo.model.Student
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentRepository: MongoRepository<Student, String>{
    fun findByEmail(email: String): Student?
}