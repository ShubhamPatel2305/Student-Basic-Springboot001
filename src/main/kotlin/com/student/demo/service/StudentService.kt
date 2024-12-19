package com.student.demo.service

import com.opencsv.CSVReader
import com.student.demo.model.Gender
import com.student.demo.model.Student
import com.student.demo.repository.StudentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.util.UUID

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.student.demo.repository.CustomStudentRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    @Autowired private val emailService: EmailService,
    private val customStudentRepository: CustomStudentRepository,
    private val restTemplate: RestTemplate
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

        emailService.sendMail("Admission Success!!!","You have been sucessfully added to XYZ School",savedStudent.email)
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

    fun addStudentsByCSV(file: MultipartFile): ResponseEntity<Any> {
        val existingStudents = mutableListOf<Student>()
        val addedStudents = mutableListOf<Student>()

        try {
            // Use OpenCSV to read the file
            val reader = CSVReader(InputStreamReader(file.inputStream))

            // Read headers
            val header = reader.readNext() ?: throw IllegalArgumentException("Empty CSV file")

            // Debug: Print actual headers
            println("Actual Headers: ${header.toList()}")

            // Validate header (case-insensitive)
            val expectedHeaders = listOf("name", "email", "classtag", "age", "gender")
            val lowercaseHeaders = header.map { it.lowercase().trim() }

            println("Lowercase Headers: $lowercaseHeaders")
            println("Expected Headers: $expectedHeaders")

            // Check if all expected headers are present
            val missingHeaders = expectedHeaders.filter { it !in lowercaseHeaders }
            if (missingHeaders.isNotEmpty()) {
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "Invalid CSV format",
                    "missing_headers" to missingHeaders,
                    "actual_headers" to lowercaseHeaders
                ))
            }

            // Get header indices dynamically
            val headerMap = lowercaseHeaders.withIndex().associate { it.value to it.index }

            // Process each row
            var line: Array<String>?
            while (reader.readNext().also { line = it } != null) {
                // Create Student object
                val student = Student(
                    id = UUID.randomUUID().toString(),
                    name = line!![0].trim(),
                    email = line!![1].trim(),
                    classTag = line!![2].trim(),
                    age = line!![3].trim().toByte(),
                    gender = Gender.valueOf(line!![4].trim().uppercase())
                )

                // Check if student with email already exists
                val existingStudent = studentRepository.findByEmail(student.email)

                if (existingStudent == null) {
                    studentRepository.save(student)
                    addedStudents.add(student)

                    // Send email for each successful addition
                    emailService.sendMail(
                        "Admission Success!!!",
                        "You have been successfully added to XYZ School",
                        student.email
                    )
                } else {
                    existingStudents.add(student)
                }
            }

            // Prepare response
            return if (existingStudents.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(mapOf(
                    "message" to "Some students could not be added due to existing emails",
                    "added" to addedStudents.size,
                    "duplicates" to existingStudents.map { it.email }
                ))
            } else {
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                    "message" to "All students added successfully",
                    "added" to addedStudents.size
                ))
            }

        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing CSV: ${e.message}")
        }
    }

    fun generateStudentDetailsPDF(studentId: String): ByteArray {
        // Find student
        val student = studentRepository.findById(studentId)
            .orElseThrow { NoSuchElementException("Student not found with ID: $studentId") }

        // Create PDF
        val outputStream = ByteArrayOutputStream()

        try {
            val document = Document()
            PdfWriter.getInstance(document, outputStream)

            document.open()

            // Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val title = Paragraph("Student Details", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)

            // Add some spacing
            document.add(Paragraph(" "))

            // Student Details Table
            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(40f, 60f))

            // Helper function to add rows
            fun addRow(label: String, value: String) {
                val labelCell = PdfPCell(Phrase(label))
                labelCell.border = Rectangle.BOX
                labelCell.backgroundColor = BaseColor.LIGHT_GRAY

                val valueCell = PdfPCell(Phrase(value))
                valueCell.border = Rectangle.BOX

                table.addCell(labelCell)
                table.addCell(valueCell)
            }

            // Add student details
            addRow("Student ID", student.id ?: "N/A")
            addRow("Name", student.name)
            addRow("Email", student.email)
            addRow("Class", student.classTag)
            addRow("Age", student.age.toString())
            addRow("Gender", student.gender.toString())
            addRow("Generated On", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

            document.add(table)

            document.close()
        } catch (e: Exception) {
            throw RuntimeException("Error generating PDF: ${e.message}")
        }

        // Return PDF bytes
        return outputStream.toByteArray()
    }

    fun listStudents(
        name: String?,
        age: Int?,
        classTag: String?,
        gender: String?,
        email: String?,
        page:Int,
        pageSize: Int
    ): List<Student>{
        val students=customStudentRepository.searchAndFilter(
            name=name,
            age=age,
            classTag=classTag,
            gender=gender,
            page = page,
            pageSize = pageSize,
            email= email
        )

        return students
    }

    fun signIn(email: String, password: String) {
        val url = "https://teamsync-infosys-internship-oct2024-9gij.vercel.app/user/signin"
        val headers = HttpHeaders()
        headers.set("token", "abc")
        headers.contentType = MediaType.APPLICATION_JSON

        val request = HttpEntity(mapOf("email" to email, "password" to password), headers)

        try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)

            if (response.statusCode == HttpStatus.OK) {
                val token = response.body?.get("token")
                println("Token: $token")
            } else if (response.statusCode == HttpStatus.BAD_REQUEST) {
                val errors = response.body?.get("errors")
                println("Error: $errors")
            }
        } catch (e: Exception) {
            println("Exception occurred: ${e.message}")
        }
    }
}