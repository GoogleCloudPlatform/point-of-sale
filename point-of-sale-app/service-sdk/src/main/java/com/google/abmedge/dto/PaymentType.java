<<<<<<< HEAD:point-of-sale-app/service-sdk/src/main/java/com/google/abmedge/dto/PaymentType.java
package com.google.abmedge.dto;
=======
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
>>>>>>> add-payment-apis:point-of-sale-app/payments/src/main/java/com/google/abmedge/payments/dto/PaymentType.java

public enum PaymentType {
  CARD("CARD"),
  CASH("CASH");

  private String type;

  PaymentType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
