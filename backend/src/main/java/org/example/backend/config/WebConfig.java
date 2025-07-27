package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins(
//                        "http://localhost:5173",   // Vite (http)
//                        "https://localhost:5173",  // Vite (https)
//                        "http://localhost:5174",
//                        "https://localhost:5174",
//                        "http://localhost:8080"    // Swagger UI
//                )
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // OPTIONS도 허용 필수
//                .allowedHeaders("*")
//                .allowCredentials(true);
    }
}
