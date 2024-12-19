package com.student.demo.repository

import com.student.demo.model.Student
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class CustomStudentRepository(private val mongoTemplate: MongoTemplate) {
    fun searchAndFilter(
        name: String?,
        age: Int?,
        classTag: String?,
        gender: String?,
        email: String?,
        page: Int,
        pageSize: Int
    ): List<Student> {
        val query = Query()
        if (!name.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("name").regex(name, "i"))
        }
        if (age != null) {
            query.addCriteria(Criteria.where("age").`is`(age))
        }
        if (!classTag.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("classTag").`is`(classTag))
        }
        if (!gender.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("gender").`is`(gender))
        }
        if (!email.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("email").regex(email,"i"))
        }

        // Add pagination
        query.skip((page - 1L) * pageSize).limit(pageSize)

        return mongoTemplate.find(query, Student::class.java)
    }
}