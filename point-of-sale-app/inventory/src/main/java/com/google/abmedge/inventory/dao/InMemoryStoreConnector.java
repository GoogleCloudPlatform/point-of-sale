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

package com.google.abmedge.inventory.dao;

import com.google.abmedge.inventory.util.InventoryStoreConnectorException;
import java.util.HashMap;
import com.google.abmedge.dto.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An implementation of the {@link InventoryStoreConnector} that uses an in memory data structure to
 * hold all the inventory information. This implementation uses a simple HashMap to store all the
 * items
 */
public class InMemoryStoreConnector implements InventoryStoreConnector {

  private static final Logger LOGGER = LogManager.getLogger(InMemoryStoreConnector.class);
  private static final Map<UUID, Item> idToItemsMap = new ConcurrentHashMap<>();
  private static final Map<String, List<Item>> typeToItemsMap = new ConcurrentHashMap<>();

  @Override
  public List<Item> getAll() {
    return idToItemsMap.values().stream().map(Item::from).collect(Collectors.toList());
  }

  @Override
  public List<Item> getAllByType(String type) {
    return typeToItemsMap.get(type);
  }

  @Override
  public Optional<Item> getById(UUID id) {
    return idToItemsMap.values().stream()
        .filter(i -> i.getId().equals(id))
        .map(Item::from)
        .findAny();
  }

  @Override
<<<<<<< HEAD
  public Set<String> getTypes() {
    return typeToItemsMap.keySet();
  }

  @Override
  public boolean insert(Item item) {
=======
  public void insert(Item item) throws InventoryStoreConnectorException {
>>>>>>> ui
    UUID itemId = item.getId();
    String itemType = item.getType();
    if (itemId == null || StringUtils.isEmpty(itemType)) {
      String errMsg =
          "Cannot insert item. Item must have both 'id' and 'type'. "
              + "Passed in item is missing one or both of these attributes";
      LOGGER.error(errMsg);
      throw new InventoryStoreConnectorException(errMsg);
    }
    Item toInsertItem = Item.from(item);
    idToItemsMap.put(itemId, toInsertItem);
<<<<<<< HEAD
    List<Item> itemsOfType = typeToItemsMap.computeIfAbsent(itemType, k -> new ArrayList<>());
    itemsOfType.add(toInsertItem);
    return true;
=======
>>>>>>> ui
  }

  @Override
  public void insert(List<Item> items) throws InventoryStoreConnectorException {
    for (Item it : items) {
<<<<<<< HEAD
      boolean inserted = insert(it);
      if (!inserted) {
        LOGGER.error(
            "Issue inserting items. Items must have both 'id' and 'type'. "
                + "One of the passed in item is missing one or both of these attributes");
        return false;
      }
    }
    return true;
=======
      UUID itemId = it.getId();
      String itemType = it.getType();
      if (itemId == null || StringUtils.isEmpty(itemType)) {
        String errMsg =
            "Cannot insert items. Items must have both 'id' and 'type'. "
                + "One of the passed in item is missing one or both of these attributes";
        LOGGER.error(errMsg);
        throw new InventoryStoreConnectorException(errMsg);
      }
    }
    for (Item it : items) {
      UUID itemId = it.getId();
      Item toInsertItem = Item.from(it);
      idToItemsMap.put(itemId, toInsertItem);
    }
>>>>>>> ui
  }

  @Override
  public void update(Item item) throws InventoryStoreConnectorException {
    UUID itemId = item.getId();
    String itemType = item.getType();
    if (itemId == null || StringUtils.isEmpty(itemType)) {
      String errMsg =
          "Cannot update item. Item must have both 'id' and 'type'. "
              + "Passed in item is missing one or both of these attributes";
      LOGGER.error(errMsg);
      throw new InventoryStoreConnectorException(errMsg);
    }
    Item toUpdateItem = Item.from(item);
    idToItemsMap.put(itemId, toUpdateItem);
  }

  @Override
  public void delete(UUID id) throws InventoryStoreConnectorException {
    if (id == null) {
      String errMsg = "Cannot delete item. Item 'id' cannot be null";
      LOGGER.error(errMsg);
      throw new InventoryStoreConnectorException(errMsg);
    }
    Item removed = idToItemsMap.remove(id);
  }

  @Override
  public void delete(List<UUID> ids) {
    for (UUID id : ids) {
      if (id == null) {
        LOGGER.warn("Cannot delete item. Skipping NULL values 'id'");
        continue;
      }
      idToItemsMap.remove(id);
    }
  }
}
