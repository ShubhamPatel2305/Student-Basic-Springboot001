package com.student.demo.service

import com.student.demo.model.Student
import com.student.demo.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StudentService(
    private val studentRepository : StudentRepository
) {
    fun addStudent(student: Student): ResponseEntity<Any>{
        //check if student with same email already exists
        val existingStudent = studentRepository.findByEmail(student.email)

        if(existingStudent != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists.")
        }
        //if not add him to db by generating random uuid
        val studentWithUUId= student.copy(
            id=UUID.randomUUID().toString()
        )
        val savedStudent= studentRepository.save(studentWithUUId)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent)
    }

    fun updateStudent(id: String, student: Student): ResponseEntity<Any> {
        return try {
            // Try to find the student by ID
            val existingStudentOptional = studentRepository.findById(id)

            if (existingStudentOptional.isPresent) {

                // Get the student from the Optional
                val existingStudent = existingStudentOptional.get()

                // Update the student's details, keeping the same ID and updating other fields
                val updatedStudent = existingStudent.copy(
                    name = student.name,
                    email = student.email,
                    classTag = student.classTag,
                    age = student.age,
                    gender = student.gender
                )

                // Save the updated student to the database
                val savedStudent = studentRepository.save(updatedStudent)

                // Return a success response with the updated student
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(savedStudent)
            } else {
                // If the student doesn't exist, return a 404 Not Found response
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Student with ID $id not found")
            }
        } catch (e: Exception) {
            // Catch any other exceptions and return a 500 Internal Server Error response
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating the student: ${e.message}")
        }
    }

    fun deleteStudent(id: String): ResponseEntity<Any> {
        return try {
            val existingStudentOptional = studentRepository.findById(id)

            if (existingStudentOptional.isPresent) {
                studentRepository.delete(existingStudentOptional.get())

                ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body("Student with ID $id deleted successfully")
            } else {
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Student with ID $id not found")
            }
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting the student: ${e.message}")
        }
    }

}