package com.google.abmedge.apiserver.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @SuppressWarnings("NullableProblems")
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/*").allowedOrigins("*");
      }
    };
  }
}