/**
 * Copyright 2021 Google LLC
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

const API_SERVER_URL = (process.env.VUE_APP_API_SERVER_URL) ?
  process.env.VUE_APP_API_SERVER_URL : window.location.origin;
const HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type',
  'Content-Type': 'application/json'
};

function post(path, headers, body) {
  const promise = fetch(path, {
    method: 'POST',
    body: JSON.stringify(body),
    headers,
  });
  return promise.then((response) => response);
}

function get(path, headers) {
  const promise = fetch(path, {
    method: 'GET',
    headers,
  });
  return promise.then((response) => response);
}

export default {
  async pay(payRequest) {
    return post(`${API_SERVER_URL}/api/pay`, HEADERS, payRequest);
  },
  async items() {
    return get(`${API_SERVER_URL}/api/items`, HEADERS);
  },
  async types() {
    return get(`${API_SERVER_URL}/api/types`, HEADERS);
  }
}
