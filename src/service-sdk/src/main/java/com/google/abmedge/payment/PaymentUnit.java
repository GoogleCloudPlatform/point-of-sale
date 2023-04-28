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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * This class represents a payment for a specific item that is being purchased. It contains
 * information such as the item, the number of it being purchased and the total cost for all of
 * them. Usually a {@link Payment} includes a collection of {@link PaymentUnit}s making up a bill
 * which shows multiple purchased items.
 */
@Entity
@Table(name = PaymentUnit.PAYMENT_UNIT_TABLE)
public class PaymentUnit implements Serializable {

  public static final String PAYMENT_UNIT_TABLE = "payment_units";

  @Id
  @Column(columnDefinition = "CHAR(36)")
  private UUID id = UUID.randomUUID();

  @Column(columnDefinition = "CHAR(36)")
  private UUID itemId;

  private String name;
  private BigDecimal quantity;
  private BigDecimal totalCost;

  @Version private Long version;

  public PaymentUnit() {}

  public PaymentUnit(UUID itemId, String name, BigDecimal quantity, BigDecimal totalCost) {
    this.itemId = itemId;
    this.name = name;
    this.quantity = quantity;
    this.totalCost = totalCost;
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
    this.quantity = quantity;
  }

  public BigDecimal getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(BigDecimal totalCost) {
    this.totalCost = totalCost;
  }
}
