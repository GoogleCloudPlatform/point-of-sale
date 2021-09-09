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

import com.google.abmedge.payments.dto.Bill;
import com.google.abmedge.payments.dto.Payment;
import com.google.abmedge.payments.dto.PaymentStatus;
import com.google.abmedge.payments.dto.PaymentUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InMemoryPaymentGateway implements PaymentGateway {

  private static final Logger LOGGER = LogManager.getLogger(InMemoryPaymentGateway.class);
  private static final Map<UUID, Payment> paymentMap = new ConcurrentHashMap<>();
  private static final String BILL_HEADER =
      "----------------------------------------------------------------------------\n";
  private static final String SPACE = " ";
  private static final String TOTAL = "  Total:";
  private static final String PAID = "  Paid:";
  private static final String BALANCE = "  Balance:";

  @Override
  public Bill pay(Payment payment) {
    UUID pId = payment.getId();
    paymentMap.put(pId, payment);
    Pair<String, String> generatedBill = generateBill(pId, payment);
    return new Bill()
        .setPayment(payment)
        .setStatus(PaymentStatus.SUCCESS)
        .setBalance(Double.valueOf(generatedBill.getRight()))
        .setPrintedBill(generatedBill.getLeft());
  }

  private Pair<String, String> generateBill(UUID paymentId, Payment payment) {
    float total = 0;
    StringBuilder billBuilder = new StringBuilder();
    billBuilder.append(billHeader(paymentId));
    // append an entry per purchase item to the bill
    int itemIndex = 1;
    for (PaymentUnit pu : payment.getUnitList()) {
      billBuilder.append(billItem(itemIndex, pu));
      total += pu.getTotalCost().floatValue();
      itemIndex++;
    }
    billBuilder.append(BILL_HEADER);
    float paid = payment.getPaidAmount().floatValue();
    float balance = paid - total;
    billBuilder.append(infoLine(TOTAL, total));
    billBuilder.append(infoLine(PAID, paid));
    billBuilder.append(infoLine(BALANCE, balance));
    billBuilder.append(BILL_HEADER);
    LOGGER.info(String.format("Processed payment:\n%s", billBuilder));
    //  ----------------------------------------------------------------------------
    //                Payment id: 02beba81-e19f-4543-9823-261db722ed02
    //  ----------------------------------------------------------------------------
    //      1. 5x BigBurger (02beba81-e19f-4543-9823-261db722ed02):           $34.44
    //      2. 4x DoubleBurger (4df41297-a96f-4602-8059-df3b0e4071cb):         $21.2
    //  ----------------------------------------------------------------------------
    //    Total:                                                              $55.64
    //    Paid:                                                             $5000.00
    //    Balance:                                                          $4944.36
    //  ----------------------------------------------------------------------------
    return Pair.of(billBuilder.toString(), String.format("%.2f", balance));
  }

  private StringBuilder billHeader(UUID paymentId) {
    StringBuilder sb = new StringBuilder();
    sb.append(BILL_HEADER);
    sb.append(String.format("              Payment id: %s              \n", paymentId));
    sb.append(BILL_HEADER);
    return sb;
  }

  private StringBuilder billItem(int itemIndex, PaymentUnit paymentUnit) {
    StringBuilder sb = new StringBuilder();
    UUID unitId = paymentUnit.getItemId();
    String unitName = paymentUnit.getName();
    Number totalUnitValue = paymentUnit.getTotalCost();
    long unitQuantity = paymentUnit.getQuantity();
    String leadingStr =
        String.format("    %s. %sx %s (%s):", itemIndex, unitQuantity, unitName, unitId);
    // get length of the current line so far
    int leadingLength = leadingStr.length();
    // get length of the total cost for this item
    int costLength = totalUnitValue.toString().length();
    // calculate the number of spaces between the item description and the total cost
    // -2 for the dollar $ sign and the newline
    int middleSpaces = BILL_HEADER.length() - leadingLength - costLength - 2;
    sb.append(leadingStr);
    sb.append(spaces(middleSpaces));
    sb.append(String.format("$%s\n", totalUnitValue));
    return sb;
  }

  private StringBuilder infoLine(String infoType, float value) {
    StringBuilder sb = new StringBuilder();
    String formattedValue = String.format("%.2f", value);
    int spacesToAdd = BILL_HEADER.length() - infoType.length() - formattedValue.length() - 2;
    sb.append(infoType);
    sb.append(spaces(spacesToAdd));
    sb.append(String.format("$%s\n", formattedValue));
    return sb;
  }

  private StringBuilder spaces(int count) {
    StringBuilder sb = new StringBuilder();
    while (count > 0) {
      sb.append(SPACE);
      count--;
    }
    return sb;
  }
}
