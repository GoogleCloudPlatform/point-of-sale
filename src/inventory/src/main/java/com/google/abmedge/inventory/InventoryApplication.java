// Copyright 2022 Google LLC
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

package com.google.abmedge.inventory;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The main entry point into the inventory server of the point-of-sale application stack. This class
 * serves the key requirement of starting the Springboot service and the embedded web server along
 * wih to start accepting requests
 *
 * <p>The inventory service APIs enable accessing the items available for serving via the
 * point-of-sale application. The inventory service communicates to the configured datastore to load
 * the items. The APIs of the inventory service are not exposed publicly.
 */
@SpringBootApplication
@EntityScan(basePackages={"com.google.abmedge.inventory"})
@ComponentScan(basePackages={"com.google.abmedge.inventory"})
@EnableJpaRepositories(basePackages={"com.google.abmedge.inventory"})
public class InventoryApplication {
  private static final Logger LOGGER = LogManager.getLogger(InventoryApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(InventoryApplication.class, args);
  }

  /** A utility method to print out a log message when the Springboot application terminates */
  @PreDestroy
  public void destroy() {
    LOGGER.info("InventoryApplication is shutting down");
  }
}
