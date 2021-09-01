package com.google.abmedge.inventory;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryApplication {
	private static final Logger LOGGER =
			LogManager.getLogger(InventoryApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(InventoryApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("InventoryApplication is shutting down");
	}
}
