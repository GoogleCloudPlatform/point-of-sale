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

import com.google.abmedge.dto.PurchaseItem;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the main controller class for the frontend service defines the APIs exposed by the
 * service. The controller also defines 2 APIs (/ready and /healthy) for readiness and health
 * checkups
 */
@RestController
public class FrontendController {

  private static final Logger LOGGER =
      LogManager.getLogger(FrontendController.class);
  private static final String INVENTORY_EP_ENV = "INVENTORY_EP";
  private static final String PAYMENTS_EP_ENV = "PAYMENTS_EP";
  private static final String FAILED = "Failed to fetch items; try again after a while";
  private static final String ITEMS_EP = "/items";
  private static final String SWITCH_EP = "/switch";
  private static final String UPDATE_EP = "/update";
  private static final String PAY_EP = "/pay";
  private static String INVENTORY_SERVICE = "http://inventory-svc:8080";
  private static String PAYMENTS_SERVICE = "http://payments-svc:8080";
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(10))
      .build();

  @PostConstruct
  void init() {
    initServiceEndpoints();
  }

  @RequestMapping("/")
  public String home() {
    return "Hello Anthos BareMetal - Frontend Controller";
  }

  @GetMapping(value = "/home", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> items() {
    String itemsEndpoint = INVENTORY_SERVICE + ITEMS_EP;
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .GET()
          .uri(URI.create(itemsEndpoint))
          .build();
      HttpResponse<String> response = HTTP_CLIENT
          .send(request, HttpResponse.BodyHandlers.ofString());
      int statusCode = response.statusCode();
      if (statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.NO_CONTENT.value()) {
        String responseItems = response.body();
        LOGGER.info(String.format("Inventory service response for endpoint '%s' is: \n%s",
            itemsEndpoint, responseItems));
        return new ResponseEntity<>(responseItems, HttpStatus.OK);
      }
      LOGGER.error(
          String.format("Failed to fetch items list from '%s'. Status code '%s'",
              itemsEndpoint, statusCode));
    } catch (IOException | InterruptedException e) {
      LOGGER.error(
          String.format("Failed to fetch items list from '%s'", itemsEndpoint), e);
    }
    return new ResponseEntity<>(FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PostMapping(value = "/switch/{type}")
  public ResponseEntity<Void> switchType(@PathVariable String type) {
    String switchEndpoint = INVENTORY_SERVICE + SWITCH_EP + "/" + type;
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .POST(HttpRequest.BodyPublishers.noBody())
          .uri(URI.create(switchEndpoint))
          .build();
      HttpResponse<String> response = HTTP_CLIENT
          .send(request, HttpResponse.BodyHandlers.ofString());
      int statusCode = response.statusCode();
      if (statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.NO_CONTENT.value()) {
        return new ResponseEntity<>(HttpStatus.OK);
      }
      LOGGER.error(String.format("Failed to switch active inventory type to '%s' via endpoint '%s'."
          + " Status code '%s'", type, switchEndpoint, statusCode));
    } catch (IOException | InterruptedException e) {
      LOGGER.error(
          String.format("Failed to switch active inventory type to '%s' via endpoint '%s'.", type,
              switchEndpoint), e);
    }
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PostMapping(value = "/pay")
  public ResponseEntity<Void> pay(@RequestBody List<PurchaseItem> purchaseList) {
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Readiness probe endpoint.
   *
   * @return HTTP Status 200 if server is ready to receive requests.
   */
  @GetMapping("/ready")
  @ResponseStatus(HttpStatus.OK)
  public String readiness() {
    return "ok";
  }

  /**
   * Liveness probe endpoint.
   *
   * @return HTTP Status 200 if server is healthy and serving requests.
   */
  @GetMapping("/healthy")
  public ResponseEntity<String> liveness() {
    // TODO:: Add suitable liveness check
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  private void initServiceEndpoints() {
    String inventory = System.getenv(INVENTORY_EP_ENV);
    String payments = System.getenv(PAYMENTS_EP_ENV);
    if (StringUtils.isNotBlank(inventory)) {
      LOGGER.info(String.format("Setting inventory service endpoint to: %s", inventory));
      INVENTORY_SERVICE = inventory;
    } else {
      LOGGER.warn(String.format("Could not read environment variable %s; thus defaulting to %s",
          INVENTORY_EP_ENV, INVENTORY_SERVICE));
    }
    if (StringUtils.isNotBlank(payments)) {
      LOGGER.info(String.format("Setting payments service endpoint to: %s", payments));
      PAYMENTS_SERVICE = payments;
    } else {
      LOGGER.warn(String.format("Could not read environment variable %s; thus defaulting to %s",
          PAYMENTS_EP_ENV, PAYMENTS_SERVICE));
    }
  }
}
