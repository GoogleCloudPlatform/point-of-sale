package com.google.abmedge.apiserver;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class ApiServerApplication {
	private static final Logger LOGGER =
			LogManager.getLogger(ApiServerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApiServerApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("Api-Server is shutting down");
	}
}
