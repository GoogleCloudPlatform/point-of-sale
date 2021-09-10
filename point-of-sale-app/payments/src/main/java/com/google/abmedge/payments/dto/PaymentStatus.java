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

/**
 * This enum is holds a list of statuses that the different {@link
 * com.google.abmedge.payments.dao.PaymentGateway} implementations may use on the {@link Bill}
 * object they return.
 */
public enum PaymentStatus {
  SUCCESS("SUCCESS"),
  FAILED("FAILED");

  private String status;

  PaymentStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
