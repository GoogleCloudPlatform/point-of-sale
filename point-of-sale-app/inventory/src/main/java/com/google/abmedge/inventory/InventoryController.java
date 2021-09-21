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

package com.google.abmedge.inventory;

import com.google.abmedge.dto.Item;
import com.google.abmedge.dto.PurchaseItem;
import com.google.abmedge.inventory.dao.InMemoryStoreConnector;
import com.google.abmedge.inventory.dao.InventoryStoreConnector;
import com.google.abmedge.inventory.dto.Inventory;
import com.google.abmedge.inventory.util.InventoryStoreConnectorException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * This is the main controller class for the inventory service defines the APIs exposed by the
 * service. The controller also defines 2 APIs (/ready and /healthy) for readiness and health
 * checkups
 */
@RestController
public class InventoryController {

  private static final Logger LOGGER = LogManager.getLogger(InventoryController.class);
  private static final String CONNECTOR_TYPE_ENV_VAR = "CONNECTOR";
  private static final String ACTIVE_TYPE_ENV_VAR = "ACTIVE_ITEM_TYPE";
  private static final String INVENTORY_ITEMS_ENV_VAR = "ITEMS";
  private static final String IN_MEMORY_CONNECTOR = "IN_MEMORY";
  private static final String ALL_ITEMS = "ALL";
  private static final Gson GSON = new Gson();
  private static final Map<String, InventoryStoreConnector> inventoryMap =
      new HashMap<>() {
        {
          put(IN_MEMORY_CONNECTOR, new InMemoryStoreConnector());
        }
      };
  // the context of the inventory service (e.g. textile, food, electronics, etc)
  private String activeItemsType;
  private InventoryStoreConnector activeConnector;

  /**
   * This method runs soon after the object for this class is created on startup of the
   * application.
   */
  @PostConstruct
  void init() {
    initConnectorType();
    initItemsType();
    initInventoryItems();
  }

  @RequestMapping("/")
  public String home() {
    return "Hello Anthos BareMetal - Inventory Controller";
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

  @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> items() {
    List<Item> inventoryItems =
        activeItemsType.equals(ALL_ITEMS)
            ? activeConnector.getAll()
            : activeConnector.getAllByType(activeItemsType);
    String jsonString = GSON.toJson(inventoryItems, new TypeToken<List<Item>>() {
    }.getType());
    return new ResponseEntity<>(jsonString, HttpStatus.OK);
  }

  @PostMapping(value = "/items_by_id",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> items(@RequestBody List<String> idList) {
    List<Item> inventoryItems = idList.stream()
        .map(id -> activeConnector.getById(UUID.fromString(id)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
    String jsonString = GSON.toJson(inventoryItems, new TypeToken<List<Item>>() {
    }.getType());
    return new ResponseEntity<>(jsonString, HttpStatus.OK);
  }

  /**
   * Every item managed by the inventory service is expected to have a 'type'. The type is used to
   * group items into a single context. The grouping can be based on any aspect that makes sense for
   * the deployment (e.g. textile, food, electronics, etc).
   *
   * <p>This method takes in a specific inventory type and changes the current context of the
   * inventory service to that specific type by setting the {@link InventoryController#activeItemsType}
   * variable. The inventory service APIs respond to requests by only loading and looking at the
   * items in the inventory that match the current active 'type'.
   *
   * @param type the type to which the current context is to be switched to
   * @return HTTP 200 if the switch is done without any errors
   */
  @PostMapping(value = "/switch/{type}")
  public ResponseEntity<Void> switchType(@PathVariable String type) {
    this.activeItemsType = type;
    LOGGER.info(
        String.format("The active inventory type has been changed to: %s", activeItemsType));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * This method serves as the mapping of the '/update' API in the controller. This method takes in
   * a list of {@link PurchaseItem}s and updates them in the underlying datastore exposed via the
   * active {@link InventoryStoreConnector}. The details as to how/what the update operation does is
   * specific to the implementation of {@link InventoryStoreConnector} that is used.
   *
   * @param purchaseList a list of {@link PurchaseItem} objects that needs to be updated in the
   *                     underlying datastore
   * @return an object of {@link ResponseEntity} that only has an HTTP code without any payload
   */
  @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> update(@RequestBody List<PurchaseItem> purchaseList) {
    for (PurchaseItem pi : purchaseList) {
      UUID itemId = pi.getItemId();
      Optional<Item> loadedItem = activeConnector.getById(itemId);
      try {
        if (loadedItem.isEmpty() || !updateItem(loadedItem.get(), pi)) {
          LOGGER.warn(String.format("Update attempt with invalid item id: '%s'", itemId));
          throw new Exception();
        }
      } catch (Exception e) {
        LOGGER.error("Failed to update one or more items in the purchase list!", e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private boolean updateItem(Item item, PurchaseItem purchaseItem)
      throws InventoryStoreConnectorException {
    long currentQuantity = item.getQuantity();
    if (currentQuantity < purchaseItem.getItemCount()) {
      LOGGER.error(
          String.format(
              "Failed to update item '%s - %s'. "
                  + "The requested count '%s' is more than whats available '%s'",
              purchaseItem.getItemId(),
              item.getName(),
              purchaseItem.getItemCount(),
              currentQuantity));
      return false;
    }

    long newQuantity = currentQuantity - purchaseItem.getItemCount();
    item.setQuantity(newQuantity);
    activeConnector.update(item);
    LOGGER.info(
        String.format(
            "Updated item '%s - %s' with new quantity '%s'",
            purchaseItem.getItemId(), item.getName(), newQuantity));
    return true;
  }

  /**
   * This method initializes the connector that will be used to connect to the storage system that
   * holds all the items' information. The connector should implement the interface {@link
   * InventoryStoreConnector}. A running instance of the application shall can have multiple
   * implementations of {@link InventoryStoreConnector}. However, only one of them will be active at
   * any single given time. Once, set to change the connector type a restart of the application is
   * required.
   *
   * <p>On startup, the {@link #activeConnector} is identified by looking at the environment
   * variable 'CONNECTOR'. If either this environment variable is unset/empty or an invalid value is
   * set, then the {@link InMemoryStoreConnector} is set as the default {@link #activeConnector}.
   *
   * <p>The {@link InMemoryStoreConnector} is an implementation of the {@link
   * InventoryStoreConnector} that maintains all information about the inventory in an in-memory
   * data-structure. A connector type value that is set against the environment variable 'CONNECTOR'
   * is deemed invalid if there exists no key entry matchign that type in the {@link
   * #inventoryMap}.
   */
  private void initConnectorType() {
    String connectorType = System.getenv(CONNECTOR_TYPE_ENV_VAR);
    if (StringUtils.isBlank(connectorType) || !inventoryMap.containsKey(connectorType)) {
      LOGGER.warn(
          String.format(
              "'%s' environment variable is not set; " + "thus defaulting to: %s",
              CONNECTOR_TYPE_ENV_VAR, IN_MEMORY_CONNECTOR));
      connectorType = IN_MEMORY_CONNECTOR;
    }
    activeConnector = inventoryMap.get(connectorType);
    LOGGER.info(String.format("Active connector type is: %s", connectorType));
  }

  /**
   * This method initializes the type of items to be served via the inventory API. Each item in the
   * inventory has a type associated to it. Thus, the inventory API can be configured to serve only
   * items of a certain type. Thus, on startup the decision for which item types are to be served is
   * decided by looking at the value of the environment variable 'ACTIVE_ITEM_TYPE'.
   *
   * <p>If this environment variable is not set then by default the inventory API is configured to
   * serve all types of items without any filtering. Any value that is set for this environment
   * variable should be an exact match to the value that is set against the 'type' attribute of each
   * item that set against the 'ITEMS' environment variable as explained in {@link
   * #initInventoryItems()}
   */
  private void initItemsType() {
    activeItemsType = System.getenv(ACTIVE_TYPE_ENV_VAR);
    if (StringUtils.isBlank(activeItemsType)) {
      LOGGER.warn(
          String.format(
              "'%s' environment variable is not set; " + "thus defaulting to: %s",
              ACTIVE_TYPE_ENV_VAR, ALL_ITEMS));
      activeItemsType = ALL_ITEMS;
    }
    LOGGER.info(String.format("Active items type is: %s", activeItemsType));
  }

  /**
   * This method loads the list of supported inventory items from the environmental variable
   * 'ITEMS'. If the variable is not set or is empty then there is nothing that is added to the
   * inventory store via the {@link #activeConnector}. The expected format for the environment
   * variable's value is that of a yaml file containing multiple 'item' definitions. Once, set to
   * reload new items into the inventory a restart of the application is required.
   *
   * <pre>
   * E.g:
   *    {@code System.getenv('ITEMS')} should return something as follows:
   *    items:
   *      - name: "BigBurger"
   *        type: "burgers"
   *        price: 5.50
   *        imageUrl: "usr/lib/images/bigburger.png"
   *        quantity: 200
   *        labels: [ "retail", "restaurant", "food" ]
   *      - name: "DoubleBurger"
   *        type: "burgers"
   *        price: 7.20
   *        imageUrl: "usr/lib/images/burgers.png"
   *        quantity: 200
   *        ....
   * </pre>
   */
  private void initInventoryItems() {
    String inventoryList = System.getenv(INVENTORY_ITEMS_ENV_VAR);
    if (StringUtils.isBlank(inventoryList)) {
      LOGGER.warn(
          String.format(
              "No items found under inventory list env var '%s'", INVENTORY_ITEMS_ENV_VAR));
      return;
    }
    LOGGER.debug(inventoryList);
    Yaml yaml = new Yaml(new Constructor(Inventory.class));
    Inventory inventory = yaml.load(inventoryList);
    inventory
        .getItems()
        .forEach(
            i -> {
              i.setId(UUID.randomUUID());
              try {
                activeConnector.insert(i);
              } catch (InventoryStoreConnectorException e) {
                String errMsg =
                    String.format(
                        "Failed to insert item '%s' of type '%s'", i.getName(), i.getType());
                LOGGER.error(errMsg, e);
              }
              LOGGER.info(String.format("Inserting new item: %s", i));
            });
  }
}
