package com.google.abmedge.inventory.dao;

import com.google.abmedge.dto.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryConnector {
  List<Item> getAll();

  List<Item> getAllByType(String type);

  Optional<Item> getById(UUID id);

  boolean insert(Item item);

  boolean insert(List<Item> items);

  boolean update(Item item);

  boolean delete(UUID id);

  void delete(List<UUID> ids);
}
