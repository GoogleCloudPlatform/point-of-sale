// Copyright 2022 Google LLC
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

package com.google.abmedge.payment;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * This class represents a payment for a specific item that is being purchased. It contains
 * information such as the item, the number of it being purchased and the total cost for all of
 * them. Usually a {@link Payment} includes a collection of {@link PaymentUnit}s making up a bill
 * which shows multiple purchased items.
 */
@Table(name = PaymentUnit.PAYMENT_UNIT_TABLE)
public class PaymentUnit implements Serializable {

  public static final String PAYMENT_UNIT_TABLE = "payment_units";

  @Column(name = "payment_id")
  @PrimaryKey(keyOrder = 1)
  private UUID paymentId;

  @Column(name = "payment_unit_id")
  @PrimaryKey(keyOrder = 2)
  private UUID id;

  @Column(name = "item_id")
  private UUID itemId;

  private String name;
  private BigDecimal quantity;
  private BigDecimal totalCost;

  private Long version = 1L;

  public PaymentUnit() {
    this.id = UUID.randomUUID();
  }

  public PaymentUnit(UUID itemId, String name, BigDecimal quantity, BigDecimal totalCost) {
    this.itemId = itemId;
    this.name = name;
    this.quantity = quantity.setScale(9, RoundingMode.HALF_EVEN);
    this.totalCost = totalCost.setScale(9, RoundingMode.HALF_EVEN);
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(UUID paymentId) {
    this.paymentId = paymentId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity.setScale(9, RoundingMode.HALF_EVEN);
  }

  public BigDecimal getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(BigDecimal totalCost) {
    this.totalCost = totalCost.setScale(9, RoundingMode.HALF_EVEN);
  }
}
