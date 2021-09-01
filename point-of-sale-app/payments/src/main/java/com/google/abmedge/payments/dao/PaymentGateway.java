package com.google.abmedge.payments.dao;

import com.google.abmedge.dto.Payment;
import com.google.abmedge.payments.dto.Bill;

public interface PaymentGateway {

  Bill pay(Payment payment);
}
