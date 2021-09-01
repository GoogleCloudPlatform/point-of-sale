package com.google.abmedge.apiserver;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
