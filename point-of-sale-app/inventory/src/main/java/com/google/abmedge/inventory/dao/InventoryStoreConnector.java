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

  /**
   * This method fetches all the items from the underlying store irrespective of what type the items
   * are or what type the inventory system's context is set to serve.
   *
   * @return a complete list of all the items currently available
   */
  List<Item> getAll();

  /**
   * This method returns a list of all the items from the underlying store that matches a specific
   * type indicated by the input argument.
   *
   * @param type the type of items to be loaded from the store
   * @return a list of all items in the store that are of the type passed in as argument
   */
  List<Item> getAllByType(String type);

  /**
   * This method fetches the item from the underlying store that has the ID matching the input
   * argument to the method. The response is returned wrapped in an {@link Optional<Item>} object.
   * Thus if there is no item in the store that has the given ID then an {@link Optional#empty()}
   * instance is returned.
   *
   * @param id the ID of the item that is to be retrieved from the store
   * @return the item that has the given ID wrapped in an {@link Optional<Item>} object; if no such
   *     item exists then return {@link Optional#empty()}
   */
  Optional<Item> getById(UUID id);

  /**
   * This method takes in a list of {@link Item}s and inserts them into the underlying store. A
   * boolean value is returned indicating if the insert operation was SUCCESS or FAILURE.
   *
   * @param item the item to be inserted into the store
   * @return true if the insert operation was successful and false if not
   */
  void insert(Item item) throws InventoryStoreConnectorException;

  /**
   * This method takes in a list of {@link Item} objects and inserts them into the underlying store.
   * A boolean value is returned indicating if the insert operation was SUCCESS or FAILURE.
   *
   * @param items the list of items to be inserted into the store
   * @return true if all the items were inserted successfully; false if insertion of atleast one of
   *     the items failed
   */
  void insert(List<Item> items) throws InventoryStoreConnectorException;

  /**
   * This method takes in an {@link Item} object and carries out an update operation of that item in
   * the underlying store. How the update is carried out is implementation specific; thus please
   * look at the java-doc for the specific implementation of interest. A boolean value is returned
   * indicating if the update operation was SUCCESS or FAILURE.
   *
   * @param item the item to be updated
   * @return true if the item was successfully updated in the store and false if not
   */
  void update(Item item) throws InventoryStoreConnectorException;

  /**
   * THis method takes in the ID of an {@link Item} and deletes this item from the underlying store.
   * A boolean value is returned indicating if the deletion was SUCCESS or FAILURE. If the object is
   * not found then the response is a 'false' indicating a non-existence.
   *
   * @param id the ID of the item that is to be deleted from the store
   * @return true if the item was successfully deleted from the store and false if the item was not
   *     found in the store or if the deletion failed
   */
  void delete(UUID id) throws InventoryStoreConnectorException;

  /**
   * This method takes in a list of {@link Item} IDs and deletes them from the underlying store.
   * This method does not return any value. Thus, if some of the deletion operations failed or if
   * there were no items in the store that had certain IDs, there is no special returns.
   *
   * @param ids the list of IDs based on which items in the store are to be deleted
   */
  void delete(List<UUID> ids);
}
