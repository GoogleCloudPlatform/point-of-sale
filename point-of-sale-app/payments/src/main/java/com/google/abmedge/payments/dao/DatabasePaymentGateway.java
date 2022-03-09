/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.abmedge.payments.dao;

import com.google.abmedge.payment.Payment;
import com.google.abmedge.payment.PaymentRepository;
import com.google.abmedge.payments.dto.Bill;
import com.google.abmedge.payments.util.BillGenerator;
import com.google.abmedge.payments.util.PaymentProcessingFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An implementation of the {@link PaymentGateway} that connects to the database to persist and
 * retrieve payment information. This implementation uses an implementation of {@link
 * org.springframework.data.repository.CrudRepository} -> {@link DatabasePaymentGateway} to access
 * the DB
 */
@Component
public class DatabasePaymentGateway implements PaymentGateway {

  @Autowired
  private PaymentRepository paymentRepository;

  @Override
  public Bill pay(Payment payment) throws PaymentProcessingFailedException {
    try {
      Payment saved = paymentRepository.save(payment);
      return BillGenerator.generateBill(saved.getId(), payment);
    } catch (Exception e) {
      String msg = String.format(
          "Failed to process new payment for ['type': '%s', 'items': '%s', 'amount': '%s']",
          payment.getType(), payment.getUnitList().size(), payment.getPaidAmount());
      throw new PaymentProcessingFailedException(msg, e);
    }
  }
}
