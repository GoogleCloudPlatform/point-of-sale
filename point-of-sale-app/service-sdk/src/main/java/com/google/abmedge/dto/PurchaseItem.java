<<<<<<< HEAD:point-of-sale-app/service-sdk/src/main/java/com/google/abmedge/dto/PurchaseItem.java
package com.google.abmedge.dto;
=======
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
>>>>>>> add-payment-apis:point-of-sale-app/inventory/src/main/java/com/google/abmedge/inventory/dto/PurchaseItem.java

import java.util.UUID;

public class PurchaseItem {
  private UUID itemId;
  private long itemCount;

  public PurchaseItem() {}

  public PurchaseItem(UUID itemId, long itemCount) {
    this.itemId = itemId;
    this.itemCount = itemCount;
  }

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
