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

package com.google.abmedge.frontend;

import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point into the frontend server of the point-of-sale application stack. This class
 * serves the key requirement of starting the Springboot service and the embedded web server along
 * wih to start accepting requests
 *
 * The frontend service APIs are the only publicly-accessible APIs in stack. All requests to the
 * other services essentially pass-through this service. The static files from the UI consume the
 * frontend APIs to load items and process payment
 *
 *                                 --------------
 *                    |----------> |  inventory |
 *                    |            --------------
 *            --------------
 *  --------> |  frontend  |
 *            --------------
 *                   |             --------------
 *                   |----------> |  payments  |
 *                                --------------
 */
@SpringBootApplication
public class FrontendApplication {

  private static final Logger LOGGER =
      LogManager.getLogger(FrontendApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(FrontendApplication.class, args);
  }

  /**
   * A utility method to print out a log message when the Springboot application terminates
   */
  @PreDestroy
  public void destroy() {
    LOGGER.info("FrontendApplication is shutting down");
  }
}
