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
import com.google.abmedge.payments.util.PaymentProcessingFailedException;

/**
 * This interface explains the APIs exposed by any implementation that connects to a payment gateway
 * to process payment events.
 */
public interface PaymentGateway {

  /**
   * This method enables processing of a payment activity expressed by a {@link Payment} object. The
   * method will connect to the available underlying payment gateway, process the payment and return
   * a {@link Bill} describing the results of processing the payment.
   *
   * @param payment the {@link Payment} object containing the details of the purchase for which the
   *     payment is being processed
   * @return a {@link Bill} instance that explains the status of the payment request
   * @throws PaymentProcessingFailedException in the event of any failures or errors with processing
   *     the payment through the payment gateway
   */
  Bill pay(Payment payment) throws PaymentProcessingFailedException;
}
