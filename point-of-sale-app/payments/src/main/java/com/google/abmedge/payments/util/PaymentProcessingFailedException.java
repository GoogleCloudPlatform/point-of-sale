package com.google.abmedge.payments.util;

/** A checked exception that is thrown whenever there is a failure from the payment gateway */
public class PaymentProcessingFailedException extends Exception {

  private static final long serialVersionUID = 7718828512143293558L;

  public PaymentProcessingFailedException() {
    super();
  }

  public PaymentProcessingFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public PaymentProcessingFailedException(String message) {
    super(message);
  }

  public PaymentProcessingFailedException(Throwable cause) {
    super(cause);
  }
}
