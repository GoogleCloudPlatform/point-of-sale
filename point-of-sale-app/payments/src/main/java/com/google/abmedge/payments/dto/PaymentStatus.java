package com.google.abmedge.payments.dto;

public enum PaymentStatus {
  SUCCESS("SUCCESS"),
  FAILED("FAILED");

  private String status;

  PaymentStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
