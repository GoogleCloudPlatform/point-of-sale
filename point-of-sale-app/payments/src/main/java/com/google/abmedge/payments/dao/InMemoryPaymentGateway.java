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
    Bill paymentBill = new Bill();
    paymentBill.setPayment(payment);
    paymentBill.setStatus(PaymentStatus.SUCCESS);
    paymentBill.setBalance(Double.valueOf(generatedBill.getRight()));
    paymentBill.setPrintedBill(generatedBill.getLeft());
    return paymentBill;
  }

  private Pair<String, String> generateBill(UUID pId, Payment payment) {
    StringBuilder bill = new StringBuilder();
    float totalCost = 0;
    int count = 1;
    int lineLength = BILL_HEADER.length();

    bill.append(BILL_HEADER);
    bill.append(String.format("              Payment id: %s              \n", pId));
    bill.append(BILL_HEADER);
    for (PaymentUnit pu : payment.getUnitList()) {
      String leadingStr = String.format("    %s. %sx %s (%s):",
          count, pu.getQuantity(), pu.getName(), pu.getItemId());
      int leadingLength = leadingStr.length();
      int costLength = pu.getTotalCost().toString().length();
      // -2 for the dollar $ sign and the newline
      int middleSpaces = lineLength - leadingLength - costLength - 2;
      bill.append(leadingStr);
      fillSpaces(bill, middleSpaces);
      bill.append(String.format("$%s\n", pu.getTotalCost()));
      totalCost += pu.getTotalCost().floatValue();
      count++;
    }
    float balance = payment.getPaidAmount().floatValue() - totalCost;
    bill.append(BILL_HEADER);

    String formattedTotal = String.format("%.2f", totalCost);
    String formattedBalance = String.format("%.2f", balance);

    int spacesOnTotalLine = lineLength - TOTAL.length() - formattedTotal.length() - 2;
    int spacesOnPaidLine =
        lineLength - PAID.length() - String.valueOf(payment.getPaidAmount()).length() - 2;
    int spacesOnBalLine = lineLength - BALANCE.length() - formattedBalance.length() - 2;
    bill.append(TOTAL);
    fillSpaces(bill, spacesOnTotalLine);
    bill.append(String.format("$%s\n", formattedTotal));

    bill.append(PAID);
    fillSpaces(bill, spacesOnPaidLine);
    bill.append(String.format("$%s\n", payment.getPaidAmount()));

    bill.append(BALANCE);
    fillSpaces(bill, spacesOnBalLine);
    bill.append(String.format("$%s\n", formattedBalance));
    bill.append(BILL_HEADER);
    LOGGER.info(String.format("Processed payment:\n%s", bill));
    return Pair.of(bill.toString(), formattedBalance);
  }

  private void fillSpaces(StringBuilder sb, int count) {
    while (count > 0) {
      sb.append(SPACE);
      count--;
    }
  }
}
