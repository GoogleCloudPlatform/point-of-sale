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

package com.google.abmedge.payments.dao;

import com.google.abmedge.payment.Payment;
import com.google.abmedge.payments.dto.Bill;
import com.google.abmedge.payments.dto.PaymentStatus;
import com.google.abmedge.payments.util.BillGenerator;
import com.google.abmedge.payments.util.PaymentProcessingFailedException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class is a specific implementation of the {@link PaymentGateway} interface. This class does
 * not interact with any external systems to process the payments. Instead, it uses an in-memory map
 * to keep track of all the payment events. This class acts as the default {@link
 * com.google.abmedge.payments.PaymentsController#activePaymentGateway} when either no/invalid
 * environment variable is set against the key 'PAYMENT_GW' or when it is set to 'IN_MEMORY'.
 */
public class InMemoryPaymentGateway implements PaymentGateway {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private static final Map<UUID, Payment> paymentMap = new ConcurrentHashMap<>();

  @Override
  public Bill pay(Payment payment) throws PaymentProcessingFailedException {
    try {
      UUID pId = payment.getId();
      paymentMap.put(pId, payment);
      Pair<String, String> generatedBill = BillGenerator.generateBill(pId, payment);
      return new Bill()
          .setPayment(payment)
          .setStatus(PaymentStatus.SUCCESS)
          .setBalance(new BigDecimal(generatedBill.getRight()))
          .setPrintedBill(generatedBill.getLeft());
    } catch (Exception e) {
      String msg = String.format(
          "Failed to process new payment for ['type': '%s', 'items': '%s', 'amount': '%s']",
          payment.getType(), payment.getUnitList().size(), payment.getPaidAmount());
      throw new PaymentProcessingFailedException(msg, e);
    }
  }
}
