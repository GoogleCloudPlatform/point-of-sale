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

package com.google.abmedge.dto;

import java.util.List;
import java.util.UUID;

/**
 * This class contains a single payment unit for a purchase done via the Point-Of-Sales UI. A single
 * payment can contain the purchase of multiple items describes as {@link PaymentUnit}s inside the
 * {@link #unitList} collection. An object of this class also carries information about the type of
 * payment (as defined by {@link PaymentType}) and the total amount paid.
 */
public class Payment {

  private UUID id;
  private List<PaymentUnit> unitList;
  private PaymentType type;
  private Number paidAmount;

  public Payment() {
  }

  public Payment(UUID id, List<PaymentUnit> unitList, PaymentType type, Number paidAmount) {
    this.id = id;
    this.unitList = unitList;
    this.type = type;
    this.paidAmount = paidAmount;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public List<PaymentUnit> getUnitList() {
    return unitList;
  }

  public void setUnitList(List<PaymentUnit> unitList) {
    this.unitList = unitList;
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
