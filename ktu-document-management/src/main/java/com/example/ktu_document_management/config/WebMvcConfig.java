package com.example.ktu_document_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC Configuration class.
 * Explicitly defines resource handlers to serve static files, overriding default
 * Spring Boot behaviors to ensure compatibility with manual OpenAPI/Swagger UI deployments.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Ensures the manual OpenAPI contract file in /static is reachable
    registry.addResourceHandler("/openapi.yaml")
        .addResourceLocations("classpath:/static/");

    // Manually maps the Swagger UI WebJars, bypassing auto-generation bugs in Spring 7
    registry.addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
  }
}