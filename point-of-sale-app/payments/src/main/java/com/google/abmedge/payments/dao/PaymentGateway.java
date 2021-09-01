package com.google.abmedge.payments.dao;

import com.google.abmedge.payments.dto.Bill;
import com.google.abmedge.payments.dto.Payment;

public interface PaymentGateway {

  Bill pay(Payment payment);
}
