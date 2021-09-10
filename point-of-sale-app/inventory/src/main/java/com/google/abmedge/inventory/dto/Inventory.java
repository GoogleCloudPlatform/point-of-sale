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

package com.google.abmedge.inventory.dto;

import com.google.abmedge.inventory.InventoryController;
import java.util.List;

/**
 * This class is a container to represent a yaml definition that holds all the items to be loaded
 * into the inventory on startup. This class is used to deserialize a string yaml configuration
 * loaded from an environment variable as described in {@link
 * InventoryController#initInventoryItems()}. This class contains a collection of {@link Item}s as
 * expected from the yaml definition.
 */
public class Inventory {

  private List<Item> items;

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }
}
