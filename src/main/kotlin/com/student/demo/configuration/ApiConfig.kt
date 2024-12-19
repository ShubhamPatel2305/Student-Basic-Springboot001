package com.student.demo.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ApiConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
