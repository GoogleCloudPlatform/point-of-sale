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

package com.google.abmedge.apiserver;

import com.google.abmedge.apiserver.dto.PayRequest;
import com.google.abmedge.dto.Item;
import com.google.abmedge.dto.Payment;
import com.google.abmedge.dto.PaymentType;
import com.google.abmedge.dto.PaymentUnit;
import com.google.abmedge.dto.PurchaseItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
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
 * This is the main controller class for the api-server service defines the APIs exposed by the
 * service. The controller also defines 2 APIs (/ready and /healthy) for readiness and health
 * checkups
 */
@RestController
@RequestMapping("/api")
public class ApiServerController {

  private static final Logger LOGGER = LogManager.getLogger(ApiServerController.class);
  private static final String FAILED = "Failed to fetch items; try again after a while";
  private static final String INVENTORY_EP_ENV = "INVENTORY_EP";
  private static final String PAYMENTS_EP_ENV = "PAYMENTS_EP";
  private static final String ITEMS_EP = "/items";
  private static final String TYPES_EP = "/types";
  private static final String ITEMS_BY_ID_EP = "/items_by_id";
  private static final String SWITCH_EP = "/switch";
  private static final String UPDATE_EP = "/update";
  private static final String PAY_EP = "/pay";
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(11))
      .build();
  /**
   * default service endpoints to use if they cannot be read from environment variables in {@link
   * #initServiceEndpoints()}
   */
  private static String INVENTORY_SERVICE = "http://inventory-svc:8080";
  private static String PAYMENTS_SERVICE = "http://payments-svc:8080";
  private static final Gson GSON = new Gson();


  @PostConstruct
  void init() {
    initServiceEndpoints();
  }

  @RequestMapping("/")
  public String home() {
    return "Hello Anthos BareMetal - Api-Server Controller";
  }

  @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
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

  @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> types() {
    String itemsEndpoint = INVENTORY_SERVICE + TYPES_EP;
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .GET()
          .uri(URI.create(itemsEndpoint))
          .build();
      HttpResponse<String> response = HTTP_CLIENT
          .send(request, HttpResponse.BodyHandlers.ofString());
      int statusCode = response.statusCode();
      if (statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.NO_CONTENT.value()) {
        String responseTypes = response.body();
        LOGGER.info(String.format("Inventory service response for endpoint '%s' is: \n%s",
            itemsEndpoint, responseTypes));
        return new ResponseEntity<>(responseTypes, HttpStatus.OK);
      }
      LOGGER.error(
          String.format("Failed to fetch store types from '%s'. Status code '%s'",
              itemsEndpoint, statusCode));
    } catch (IOException | InterruptedException e) {
      LOGGER.error(
          String.format("Failed to fetch store types from '%s'", itemsEndpoint), e);
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

  /**
   * This method serves as the mapping of the '/pay' API in the Springboot controller. Any requests
   * to the pay endpoint will be attempted to bind to this method by Springboot request handlers.
   * The method expects a 'JSON' request body matching the {@link PayRequest} class attributes. The
   * {@link PayRequest} object can have a JSON array of items that have the same structure as {@link
   * PurchaseItem}.
   *
   * @param payRequest a deserialized JSON object that matches the class structure of {@link
   *                   PayRequest} that contains details of the specific purchase for which a
   *                   payment is being made
   * @return the JSON serialized form of the {@link ResponseEntity} that contains an HTTP code
   * indicating the status of the request and a string body representing the result of the pay
   * event. The string body on the response can differ based on the request type and the status.
   * <pre>
   *   1. No purchase items on the request body -> an empty response body
   *   2. Failure to fetch details of the purchase items -> a simple failure message
   *   3. Failure to update the purchase items -> a simple failure message
   *   4. Fail to process the payment -> a simple failure message
   *   5. Successfully update and make payment -> the bill for the processed payment with item details
   * </pre>
   */
  @PostMapping(value = "/pay")
  public ResponseEntity<String> pay(@RequestBody PayRequest payRequest) {
    double totalCost = 0;
    String responseStr;
    List<PurchaseItem> requestedItemList = payRequest.getItems();
    List<PurchaseItem> purchaseItems = new ArrayList<>();
    List<PaymentUnit> payUnits = new ArrayList<>();
    if (requestedItemList.isEmpty()) {
      return new ResponseEntity<>("", HttpStatus.OK);
    }
    Map<String, Long> itemIdsToCountMap = requestedItemList.stream()
        .collect(Collectors.toMap(pi -> pi.getItemId().toString(), PurchaseItem::getItemCount));
    try {
      Optional<List<Item>> optionalItemList = getItemDetails(itemIdsToCountMap);
      if (optionalItemList.isEmpty()) {
        return new ResponseEntity<>(
            "Failed to fetch items details", HttpStatus.INTERNAL_SERVER_ERROR);
      }
      List<Item> itemList = optionalItemList.get();
      int totalItemsRetrieved = itemList.size();
      int totalItemsRequested = itemIdsToCountMap.size();
      if (totalItemsRetrieved != totalItemsRequested) {
        LOGGER.warn(String.format(
            "There is a mismatch between number of items requested and retrieved - %s/%s",
            totalItemsRetrieved, totalItemsRequested));
      }
      for (Item item : itemList) {
        UUID id = item.getId();
        long itemCount = itemIdsToCountMap.get(id.toString());
        double totalItemCost = itemCount * item.getPrice().doubleValue();
        totalCost += totalItemCost;
        PurchaseItem purchaseItem = new PurchaseItem(id, itemIdsToCountMap.get(id.toString()));
        PaymentUnit paymentUnit = new PaymentUnit(id, item.getName(), itemCount, totalItemCost);
        purchaseItems.add(purchaseItem);
        payUnits.add(paymentUnit);
      }
      boolean updatedItemDetails = updateItemDetails(purchaseItems);
      if (!updatedItemDetails) {
        return new ResponseEntity<>(
            "Failed to update purchased item information", HttpStatus.INTERNAL_SERVER_ERROR);
      }
      Optional<String> optionalBill = makePayment(
          payRequest.getType(), payRequest.getPaidAmount(), totalCost, payUnits);
      if (optionalBill.isEmpty()) {
        return new ResponseEntity<>(
            "Failed to process payment for the order", HttpStatus.INTERNAL_SERVER_ERROR);
      }
      responseStr = optionalBill.get();
    } catch (IOException | InterruptedException e) {
      return new ResponseEntity<>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(responseStr, HttpStatus.OK);
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

  private Optional<List<Item>> getItemDetails(Map<String, Long> itemIdsToCountMap)
      throws IOException, InterruptedException {
    String endpoint = INVENTORY_SERVICE + ITEMS_BY_ID_EP;
    String jsonString = GSON.toJson(itemIdsToCountMap.keySet(), new TypeToken<Set<String>>() {
    }.getType());
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
        .uri(URI.create(endpoint))
        .setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
    HttpResponse<String> response = HTTP_CLIENT
        .send(request, HttpResponse.BodyHandlers.ofString());
    int statusCode = response.statusCode();
    if (isSuccessResponse(statusCode)) {
      List<Item> itemDetails = GSON.fromJson(response.body(), new TypeToken<List<Item>>() {
      }.getType());
      return Optional.of(itemDetails);
    }
    LOGGER.error(String.format("Failed to fetch items details from '%s'", endpoint));
    return Optional.empty();
  }

  private boolean updateItemDetails(List<PurchaseItem> purchaseItems)
      throws IOException, InterruptedException {
    String endpoint = INVENTORY_SERVICE + UPDATE_EP;
    String jsonString = GSON.toJson(purchaseItems, new TypeToken<List<PurchaseItem>>() {
    }.getType());
    HttpRequest request = HttpRequest.newBuilder()
        .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
        .uri(URI.create(endpoint))
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    int statusCode = response.statusCode();
    if (isSuccessResponse(statusCode)) {
      return true;
    }
    LOGGER.error(String.format("Failed to update purchased item information via '%s'", endpoint));
    return false;
  }

  private Optional<String> makePayment(
      PaymentType paymentType, Number amountOnRequest, Double totalCost, List<PaymentUnit> payUnits)
      throws IOException, InterruptedException {
    String endpoint = PAYMENTS_SERVICE + PAY_EP;
    Number paidAmount = paymentType == PaymentType.CARD ? totalCost : amountOnRequest;
    Payment payment = new Payment(UUID.randomUUID(), payUnits, PaymentType.CARD, paidAmount);
    String jsonString = GSON.toJson(payment, Payment.class);
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
        .uri(URI.create(endpoint))
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    int statusCode = response.statusCode();
    if (isSuccessResponse(statusCode)) {
      return Optional.of(response.body());
    }
    LOGGER.error(String.format("Failed to process payment via '%s'", endpoint));
    return Optional.empty();
  }

  private boolean isSuccessResponse(int statusCode) {
    return String.valueOf(statusCode).startsWith("2");
  }
}
