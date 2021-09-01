package com.google.abmedge.dto;

import java.util.List;
import java.util.UUID;

public class Payment {
  private UUID id;
  private List<PaymentUnit> unitList;
  private PaymentType type;
  private Number paidAmount;

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
