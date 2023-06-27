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

package com.google.abmedge.payment;

import com.google.cloud.spring.data.spanner.repository.SpannerRepository;
import java.util.UUID;

// This will be AUTO IMPLEMENTED by Spring into a Bean called paymentRepository
// CRUD refers Create, Read, Update, Delete
public interface PaymentRepository extends SpannerRepository<Payment, UUID> {}
