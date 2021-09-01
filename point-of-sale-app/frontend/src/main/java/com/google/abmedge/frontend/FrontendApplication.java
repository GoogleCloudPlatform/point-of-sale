package com.google.abmedge.frontend;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FrontendApplication {
	private static final Logger LOGGER =
			LogManager.getLogger(FrontendApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FrontendApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("FrontendApplication is shutting down");
	}
}
