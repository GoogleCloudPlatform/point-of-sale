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

import java.util.UUID;

/**
 * An instance of this class represents a purchase unit. A purchase unit would reference an ID,
 * identifying the item in the inventory that is being purchased and a count indicating how many of
 * this item is being purchased.
 */
public class PurchaseItem {
  private UUID itemId;
  private long itemCount;

  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  public long getItemCount() {
    return itemCount;
  }

  public void setItemCount(long itemCount) {
    this.itemCount = itemCount;
  }
}
