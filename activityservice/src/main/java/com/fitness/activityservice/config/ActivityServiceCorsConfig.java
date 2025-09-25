// File: com/fitness/activityservice/config/ActivityServiceCorsConfig.java

package com.fitness.activityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ActivityServiceCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply CORS to all /api endpoints
                .allowedOrigins("http://localhost:3000") // The frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Essential: OPTIONS must be allowed
                .allowedHeaders("*")
                .allowCredentials(false); // Set to false since we're not sending credentials
    }
}