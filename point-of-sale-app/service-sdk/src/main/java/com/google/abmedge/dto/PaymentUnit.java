<<<<<<< HEAD:point-of-sale-app/service-sdk/src/main/java/com/google/abmedge/dto/PaymentUnit.java
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

package com.google.abmedge.payments.dto;
>>>>>>> add-payment-apis:point-of-sale-app/payments/src/main/java/com/google/abmedge/payments/dto/PaymentUnit.java

import java.util.UUID;

public class PaymentUnit {

  private UUID itemId;
  private String name;
  private long quantity;
  private Number totalCost;

  public PaymentUnit() {}

  public PaymentUnit(UUID itemId, String name, long quantity, Number totalCost) {
    this.itemId = itemId;
    this.name = name;
    this.quantity = quantity;
    this.totalCost = totalCost;
  }

  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getQuantity() {
    return quantity;
  }

  public void setQuantity(long quantity) {
    this.quantity = quantity;
  }

  public Number getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(Number totalCost) {
    this.totalCost = totalCost;
  }
}
