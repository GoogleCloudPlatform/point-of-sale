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

package com.google.abmedge.payments.util;

import com.google.abmedge.payment.Payment;
import com.google.abmedge.payment.PaymentUnit;
import com.google.abmedge.payment.PaymentType;
import com.google.abmedge.payments.dao.InMemoryPaymentGateway;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BillGenerator {

  private static final Logger LOGGER = LogManager.getLogger(InMemoryPaymentGateway.class);
  private static final String BILL_HEADER =
      "----------------------------------------------------------------------------\n";
  private static final String SPACE = " ";
  private static final String TOTAL = "  Total:";
  private static final String TAX = "  Tax:";
  private static final String PAID = "  Paid:";
  private static final String BALANCE = "  Balance:";
  private static final double TAX_VALUE = 0.1495;

  /**
   * This method takes in an ID identifying a payment and a {@link Payment} object and generates a
   * string representation of the bill for the payment. This method returns two things: (1) The
   * string representation of the bill and (2) the balance amount on the bill after the total cost
   * is subtracted from the paid amount. An example of the bill generated from this method is
   * provided inline below.
   *
   * @param paymentId an identifier for the {@link Payment} event to be processed
   * @param payment the {@link Payment} activity to be processed that contains all the details about
   * the items, amount and {@link PaymentType}
   * @return a string {@link Pair} of items where the {@link Pair#getLeft()} contains the string
   * representation of the bill and the {@link Pair#getRight()} contains the formatted string value
   * of balance on the bill
   */
  public static Pair<String, String> generateBill(UUID paymentId, Payment payment) {
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
    float tax = Double.valueOf(total * TAX_VALUE).floatValue();
    float paid = payment.getPaidAmount().floatValue();
    float balance = paid - total - tax;
    billBuilder.append(infoLine(TOTAL, total));
    billBuilder.append(infoLine(TAX, tax));
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
    //    Tax:                                                                 $8.31
    //    Paid:                                                             $5000.00
    //    Balance:                                                          $4936.04
    //  ----------------------------------------------------------------------------
    return Pair.of(billBuilder.toString(), String.format("%.2f", balance));
  }

  /**
   * Utility method that generates the header section of the bill.
   *
   * @param paymentId the id to be included in the header of the bill identifying this specific
   * payment
   * @return a string containing the header for the generated bill
   */
  private static String billHeader(UUID paymentId) {
    return BILL_HEADER
        + String.format("              Payment id: %s              \n", paymentId)
        + BILL_HEADER;
  }

  /**
   * Utility method that generates a single line entry for a specific item on a bill.
   *
   * @param itemIndex the place of this item in the bill so that the line starts with this number
   * @param paymentUnit the {@link PaymentUnit} object that contains details about the item in the
   * payment event for which an entry is be generated
   * @return a string that contains a line with details about the purchase of one specific item that
   * can be appended to the bill
   */
  private static String billItem(int itemIndex, PaymentUnit paymentUnit) {
    StringBuilder sb = new StringBuilder();
    UUID unitId = paymentUnit.getItemId();
    String unitName = paymentUnit.getName();
    Number totalUnitValue = paymentUnit.getTotalCost();
    BigDecimal unitQuantity = paymentUnit.getQuantity();
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
    return sb.toString();
  }

  /**
   * Utility method to add a line for some specific detail like total, balance and paid amount. The
   * method takes in a string explaining what the line is for (e.g. Total or Balance) and the value.
   * Using these two a line is generated that can be appended to the end of the bill.
   *
   * @param infoType a string explaining what the line is about which will be appended to the
   * beginning of the generated line
   * @param value the numeric value of the information line that is to be generated (e.g. the total
   * value, the balance)
   * @return the generated line with the information type at the beginning followed by spaces and
   * the numeric value at the end formatted to 2 decimal points.
   */
  private static String infoLine(String infoType, float value) {
    StringBuilder sb = new StringBuilder();
    String formattedValue = String.format("%.2f", value);
    int spacesToAdd = BILL_HEADER.length() - infoType.length() - formattedValue.length() - 2;
    sb.append(infoType);
    sb.append(spaces(spacesToAdd));
    sb.append(String.format("$%s\n", formattedValue));
    return sb.toString();
  }

  /**
   * Utility method that takes in a number and creates a concatenation of that many spaces
   *
   * @param count number indicating how many times space is to be appended
   * @return a string that is has 'count' many times spaces
   */
  private static String spaces(int count) {
    StringBuilder sb = new StringBuilder();
    while (count > 0) {
      sb.append(SPACE);
      count--;
    }
    return sb.toString();
  }
}
