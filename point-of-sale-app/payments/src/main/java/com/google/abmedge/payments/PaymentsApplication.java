// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.abmedge.payments;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The main entry point into the payments server of the point-of-sale application stack. This class
 * serves the key requirement of starting the Springboot service and the embedded web server along
 * wih to start accepting requests
 *
 * <p>The payments service APIs enable processing of payments for the point-of-sale application. The
 * APIs of the payments service are not exposed publicly.
 */
@SpringBootApplication
@EntityScan(basePackages={"com.google.abmedge.payment"})
@ComponentScan(basePackages={"com.google.abmedge.payment"})
@EnableJpaRepositories(basePackages={"com.google.abmedge.payment"})
public class PaymentsApplication {

  private static final Logger LOGGER = LogManager.getLogger(PaymentsApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(PaymentsApplication.class, args);
  }

  /** A utility method to print out a log message when the Springboot application terminates */
  @PreDestroy
  public void destroy() {
    LOGGER.info("PaymentsApplication is shutting down");
  }
}
