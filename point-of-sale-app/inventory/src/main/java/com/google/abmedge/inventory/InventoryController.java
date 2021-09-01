package com.google.abmedge.inventory;

import com.google.abmedge.inventory.dao.InMemoryConnector;
import com.google.abmedge.inventory.dao.InventoryConnector;
import com.google.abmedge.inventory.dto.Inventory;
import com.google.abmedge.inventory.dto.Item;
import com.google.abmedge.inventory.dto.PurchaseItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

@RestController
public class InventoryController {

  private static final Logger LOGGER = LogManager.getLogger(InventoryController.class);
  private static final String CONNECTOR_TYPE_ENV_VAR = "CONNECTOR";
  private static final String ACTIVE_TYPE_ENV_VAR = "ACTIVE_ITEM_TYPE";
  private static final String INVENTORY_ITEMS_ENV_VAR = "ITEMS";
  private static final String IN_MEMORY_CONNECTOR = "IN_MEMORY";
  private static final String ALL_ITEMS = "ALL";
  private static final Gson GSON = new Gson();
  private static final Map<String, InventoryConnector> inventoryMap = new HashMap<>() {{
    put(IN_MEMORY_CONNECTOR, new InMemoryConnector());
  }};
  private String activeItemsType;
  private InventoryConnector activeConnector;

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
    List<Item> inventoryItems = activeItemsType.equals(ALL_ITEMS) ?
        activeConnector.getAll() : activeConnector.getAllByType(activeItemsType);
    String jsonString = GSON.toJson(inventoryItems, new TypeToken<List<Item>>() {
    }.getType());
    return new ResponseEntity<>(jsonString, HttpStatus.OK);
  }

  @PostMapping(value = "/switch/{type}")
  public ResponseEntity<Void> switchType(@PathVariable String type) {
    this.activeItemsType = type;
    LOGGER
        .info(String.format("The active inventory type has been changed to: %s", activeItemsType));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> update(@RequestBody List<PurchaseItem> purchaseList) {
    boolean updated = true;
    for (PurchaseItem pi : purchaseList) {
      UUID itemId = pi.getItemId();
      Optional<Item> loadedItem = activeConnector.getById(itemId);
      if (loadedItem.isPresent()) {
        Item i = loadedItem.get();
        long currentQuantity = i.getQuantity();
        if (currentQuantity >= pi.getItemCount()) {
          long newQuantity = currentQuantity - pi.getItemCount();
          i.setQuantity(newQuantity);
          updated &= activeConnector.update(i);
          LOGGER.info(String.format("Updated item '%s - %s' with new quantity '%s'",
              itemId, i.getName(), newQuantity));
          continue;
        }
        LOGGER.error(String.format(
            "Failed to update item '%s - %s'. "
                + "The requested count '%s' is more than whats available '%s'",
            itemId, i.getName(), pi.getItemCount(), currentQuantity
        ));
      } else {
        LOGGER.warn(String.format("Update attempt with invalid item id: '%s'", itemId));
        updated = false;
      }
    }
    if (!updated) {
      LOGGER.error("Failed to update one or more items in the purchase list!");
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void initInventoryItems() {
    String inventoryList = System.getenv(INVENTORY_ITEMS_ENV_VAR);
    if (StringUtils.isBlank(inventoryList)) {
      LOGGER.warn(String.format(
          "No items found under inventory list env var '%s'", INVENTORY_ITEMS_ENV_VAR));
      return;
    }
    LOGGER.debug(inventoryList);
    Yaml yaml = new Yaml(new Constructor(Inventory.class));
    Inventory inventory = yaml.load(inventoryList);
    inventory.getItems().forEach(i -> {
      i.setId(UUID.randomUUID());
      activeConnector.insert(i);
      LOGGER.info(String.format("Inserting new item: %s", i.toString()));
    });
  }

  private void initConnectorType() {
    String connectorType = System.getenv(CONNECTOR_TYPE_ENV_VAR);
    if (StringUtils.isBlank(connectorType) || !inventoryMap.containsKey(connectorType)) {
      LOGGER.warn(String.format("'%s' environment variable is not set; "
          + "thus defaulting to: %s", CONNECTOR_TYPE_ENV_VAR, IN_MEMORY_CONNECTOR));
      connectorType = IN_MEMORY_CONNECTOR;
    }
    activeConnector = inventoryMap.get(connectorType);
    LOGGER.info(String.format("Active connector type is: %s", connectorType));
  }

  private void initItemsType() {
    activeItemsType = System.getenv(ACTIVE_TYPE_ENV_VAR);
    if (StringUtils.isBlank(activeItemsType)) {
      LOGGER.warn(String.format("'%s' environment variable is not set; "
          + "thus defaulting to: %s", ACTIVE_TYPE_ENV_VAR, ALL_ITEMS));
      activeItemsType = ALL_ITEMS;
    }
    LOGGER.info(String.format("Active items type is: %s", activeItemsType));
  }
}
