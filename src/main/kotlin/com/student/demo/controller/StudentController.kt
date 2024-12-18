package com.student.demo.controller

import com.student.demo.model.Student
import com.student.demo.service.StudentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/students")
class StudentController(
    private val studentService: StudentService
) {

    @PostMapping("/add")
    fun addStudent(@RequestBody student: Student): ResponseEntity<Any> {
        return studentService.addStudent(student)
    }

    @PutMapping("/update/{id}")
    fun updateStudent(@PathVariable id: String, @RequestBody student: Student): ResponseEntity<Any> {
        return studentService.updateStudent(id, student)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteStudent(@PathVariable id: String): ResponseEntity<Any> {
        return studentService.deleteStudent(id)
    }
}