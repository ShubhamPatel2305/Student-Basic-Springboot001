package com.student.demo.controller

import com.student.demo.model.Student
import com.student.demo.service.StudentService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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

    @PostMapping("/addByCSV")
    fun addByCSV(@RequestParam("file") file:MultipartFile): ResponseEntity<Any> {
//        if(!file.originalFilename?.endsWith(".csv", true)!! == true){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only CSV file can be uploaded")
//        }
        return studentService.addStudentsByCSV(file)
    }

    @GetMapping("/getDetailsPDF/{id}")
    fun getStudentDetailsPDF(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            val pdfBytes = studentService.generateStudentDetailsPDF(id)

            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=student_details_${id}.pdf")
                .body(pdfBytes)
        } catch (e: NoSuchElementException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Student not found with ID: $id")
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: ${e.message}")
        }
    }
}