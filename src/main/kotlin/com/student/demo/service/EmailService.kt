package com.student.demo.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import javax.security.auth.Subject

@Component
class EmailService(
    private val javaMailSender: JavaMailSender
) {

    fun sendMail(subject:String, msg:String, to:String):Boolean{
        val mimeMessage = javaMailSender.createMimeMessage()
        val helper= MimeMessageHelper(mimeMessage, true)

        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(msg)

        println(helper)

        try{
            javaMailSender.send(mimeMessage)
            println("haha")
            return true
        }catch(e:Exception){
            println("heheh")
            return false
        }
    }
}