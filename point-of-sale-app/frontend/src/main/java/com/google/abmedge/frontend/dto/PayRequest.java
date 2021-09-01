package com.google.abmedge.frontend.dto;

import com.google.abmedge.dto.PaymentType;
import com.google.abmedge.dto.PurchaseItem;
import java.util.List;

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
