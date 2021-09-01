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
