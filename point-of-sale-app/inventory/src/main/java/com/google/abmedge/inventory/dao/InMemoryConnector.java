package com.google.abmedge.inventory.dao;

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

public class InMemoryConnector implements InventoryConnector {

  private static final Logger LOGGER = LogManager.getLogger(InMemoryConnector.class);
  private static final Map<UUID, Item> idToItemsMap = new ConcurrentHashMap<>();
  private static final Map<String, List<Item>> typeToItemsMap = new ConcurrentHashMap<>();

  @Override
  public List<Item> getAll() {
    return idToItemsMap.values()
        .stream()
        .map(Item::from)
        .collect(Collectors.toList());
  }

  @Override
  public List<Item> getAllByType(String type) {
    return typeToItemsMap.get(type);
  }

  @Override
  public Optional<Item> getById(UUID id) {
    return idToItemsMap.values()
        .stream()
        .filter(i -> i.getId().equals(id))
        .map(Item::from)
        .findAny();
  }

  @Override
  public Set<String> getTypes() {
    return typeToItemsMap.keySet();
  }

  @Override
  public boolean insert(Item item) {
    UUID itemId = item.getId();
    String itemType = item.getType();
    if (itemId == null || StringUtils.isEmpty(itemType)) {
      LOGGER.error("Cannot insert item. Item must have both 'id' and 'type'. "
          + "Passed in item is missing one or both of these attributes");
      return false;
    }
    Item toInsertItem = Item.from(item);
    idToItemsMap.put(itemId, toInsertItem);
    List<Item> itemsOfType = typeToItemsMap.computeIfAbsent(itemType, k -> new ArrayList<>());
    itemsOfType.add(toInsertItem);
    return true;
  }

  @Override
  public boolean insert(List<Item> items) {
    for (Item it : items) {
      boolean inserted = insert(it);
      if (!inserted) {
        LOGGER.error("Issue inserting items. Items must have both 'id' and 'type'. "
            + "One of the passed in item is missing one or both of these attributes");
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean update(Item item) {
    UUID itemId = item.getId();
    String itemType = item.getType();
    if (itemId == null || StringUtils.isEmpty(itemType)) {
      LOGGER.error("Cannot update item. Item must have both 'id' and 'type'. "
          + "Passed in item is missing one or both of these attributes");
      return false;
    }
    Item toUpdateItem = Item.from(item);
    idToItemsMap.put(itemId, toUpdateItem);
    return true;
  }

  @Override
  public boolean delete(UUID id) {
    if (id == null) {
      LOGGER.error("Cannot delete item. Item 'id' cannot be null");
    }
    Item removed = idToItemsMap.remove(id);
    return removed != null;
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
