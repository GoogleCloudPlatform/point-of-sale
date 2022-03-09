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

package com.google.abmedge.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * This class contains a single payment action for a purchase done via the Point-Of-Sales UI. A
 * single payment can contain the purchase of multiple items described as {@link PaymentUnit}s
 * inside the {@link #unitList} collection. An object of this class also carries information about
 * the type of payment (as defined by {@link PaymentType}) and the total amount paid.
 */
@Entity
@Table(name = Payment.PAYMENTS_TABLE)
public class Payment implements Serializable {

  public static final String PAYMENTS_TABLE = "payments";

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Type(type = "org.hibernate.type.UUIDCharType")
  @Column(columnDefinition = "CHAR(36)")
  private UUID id;

  @OneToMany(cascade = CascadeType.ALL)
  private List<PaymentUnit> unitList;
  private PaymentType type;
  private BigDecimal paidAmount;

  @Version
  private Long version;

  public Payment() {
  }

  public Payment(List<PaymentUnit> unitList, PaymentType type, BigDecimal paidAmount) {
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

  public void setPaidAmount(BigDecimal paidAmount) {
    this.paidAmount = paidAmount;
  }
}
