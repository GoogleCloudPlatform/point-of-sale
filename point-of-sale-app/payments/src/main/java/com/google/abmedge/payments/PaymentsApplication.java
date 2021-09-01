package com.google.abmedge.payments;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentsApplication {
	private static final Logger LOGGER =
			LogManager.getLogger(PaymentsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PaymentsApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("PaymentsApplication is shutting down");
	}
}
