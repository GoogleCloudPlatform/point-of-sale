package com.google.abmedge.payments.dto;

public class Bill {
  private Payment payment;
  private PaymentStatus status;
  private Number balance;
  private String printedBill;

  public Payment getPayment() {
    return payment;
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public Number getBalance() {
    return balance;
  }

  public void setBalance(Number balance) {
    this.balance = balance;
  }

  public String getPrintedBill() {
    return printedBill;
  }

  public void setPrintedBill(String printedBill) {
    this.printedBill = printedBill;
  }
}
