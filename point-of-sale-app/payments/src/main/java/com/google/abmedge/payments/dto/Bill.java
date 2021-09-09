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

package com.google.abmedge.payments.dto;

public class Bill {
  private Payment payment;
  private PaymentStatus status;
  private Number balance;
  private String printedBill;

  public Payment getPayment() {
    return payment;
  }

  public Bill setPayment(Payment payment) {
    this.payment = payment;
    return this;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public Bill setStatus(PaymentStatus status) {
    this.status = status;
    return this;
  }

  public Number getBalance() {
    return balance;
  }

  public Bill setBalance(Number balance) {
    this.balance = balance;
    return this;
  }

  public String getPrintedBill() {
    return printedBill;
  }

  public Bill setPrintedBill(String printedBill) {
    this.printedBill = printedBill;
    return this;
  }
}
