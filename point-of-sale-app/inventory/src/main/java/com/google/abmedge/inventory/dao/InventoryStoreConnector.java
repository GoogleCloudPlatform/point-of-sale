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

import com.google.abmedge.inventory.dto.Item;
import com.google.abmedge.inventory.util.InventoryStoreConnectorException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link InventoryStoreConnector} explains the interface using which we can interact with the
 * underlying store that holds all the inventory information. Implementations of this interface may
 * be storing and retrieving items from different places.
 */
public interface InventoryStoreConnector {

  List<Item> getAll();

  List<Item> getAllByType(String type);

  Optional<Item> getById(UUID id);

  void insert(Item item) throws InventoryStoreConnectorException;

  void insert(List<Item> items) throws InventoryStoreConnectorException;

  void update(Item item) throws InventoryStoreConnectorException;

  void delete(UUID id) throws InventoryStoreConnectorException;

  void delete(List<UUID> ids);
}
