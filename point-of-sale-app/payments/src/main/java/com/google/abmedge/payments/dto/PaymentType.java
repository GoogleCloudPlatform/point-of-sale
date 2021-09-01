package com.google.abmedge.payments.dto;

public enum PaymentType {
  CARD("CARD"),
  CASH("CASH");

  private String type;

  PaymentType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
