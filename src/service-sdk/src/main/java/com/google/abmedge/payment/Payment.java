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
import com.google.cloud.spring.data.spanner.core.mapping.Interleaved;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * This class contains a single payment action for a purchase done via the Point-Of-Sales UI. A
 * single payment can contain the purchase of multiple items described as {@link PaymentUnit}s
 * inside the {@link #unitList} collection. An object of this class also carries information about
 * the type of payment (as defined by {@link PaymentType}) and the total amount paid.
 */
@Table(name = Payment.PAYMENTS_TABLE)
public class Payment implements Serializable {

  public static final String PAYMENTS_TABLE = "payments";

  @Column(name = "payment_id")
  @PrimaryKey
  private UUID paymentId;

  @Interleaved private List<PaymentUnit> unitList;

  private PaymentType type;
  private BigDecimal paidAmount;

  private Long version = 1L;

  public Payment() {
    this.paymentId = UUID.randomUUID();
  }

  public Payment(List<PaymentUnit> unitList, PaymentType type, BigDecimal paidAmount) {
    this.paymentId = UUID.randomUUID();
    unitList.forEach(u -> u.setPaymentId(this.paymentId));
    this.unitList = unitList;
    this.type = type;
    this.paidAmount = paidAmount.setScale(9, RoundingMode.HALF_EVEN);
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(UUID paymentId) {
    this.paymentId = paymentId;
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

  public void setPaidAmount(BigDecimal paidAmount) {
    this.paidAmount = paidAmount.setScale(9, RoundingMode.HALF_EVEN);
  }
}
