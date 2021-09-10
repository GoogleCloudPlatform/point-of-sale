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

package com.google.abmedge.apiserver.dto;

import com.google.abmedge.dto.PaymentType;
import com.google.abmedge.dto.PurchaseItem;
import java.util.List;

/**
 * This class represents the sgtructure of the request object that is expected to be received via
 * {@link com.google.abmedge.apiserver.ApiServerController#pay(PayRequest)} method. The class
 * defines a collection of {@link PurchaseItem}s denoting the items for which the current payment is
 * being made. It also has reference to the type of the payment (as denoted by {@link PaymentType})
 * and the amount paid.
 */
public class PayRequest {
  private List<PurchaseItem> items;
  private PaymentType type;
  private Number paidAmount;

  public List<PurchaseItem> getItems() {
    return items;
  }

  public void setItems(List<PurchaseItem> items) {
    this.items = items;
  }

  public PaymentType getType() {
    return type;
  }

  public void setType(PaymentType type) {
    this.type = type;
  }

  public Number getPaidAmount() {
    return paidAmount;
  }

  public void setPaidAmount(Number paidAmount) {
    this.paidAmount = paidAmount;
  }
}
