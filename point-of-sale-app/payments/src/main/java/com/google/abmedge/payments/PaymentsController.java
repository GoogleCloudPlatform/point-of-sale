package com.google.abmedge.payments;

import com.google.abmedge.dto.Payment;
import com.google.abmedge.payments.dao.InMemoryPaymentGateway;
import com.google.abmedge.payments.dao.PaymentGateway;
import com.google.abmedge.payments.dto.Bill;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentsController {

  private static final Logger LOGGER = LogManager.getLogger(PaymentsController.class);
  private static final String IN_MEMORY_GATEWAY = "IN_MEMORY";
  private static final String PAYMENT_GW_TYPE_ENV_VAR = "PAYMENT_GW";
  private static final Gson GSON = new Gson();
  private static final Map<String, PaymentGateway> paymentGatewayMap = new HashMap<>() {{
    put(IN_MEMORY_GATEWAY, new InMemoryPaymentGateway());
  }};
  private PaymentGateway activePaymentGateway;

  @PostConstruct
  void init() {
    initConnectorType();
  }

  @RequestMapping("/")
  public String home() {
    return "Hello Anthos BareMetal - Payments Controller";
  }

  /**
   * Readiness probe endpoint.
   *
   * @return HTTP Status 200 if server is ready to receive requests.
   */
  @GetMapping("/ready")
  @ResponseStatus(HttpStatus.OK)
  public String readiness() {
    return "ok";
  }

  /**
   * Liveness probe endpoint.
   *
   * @return HTTP Status 200 if server is healthy and serving requests.
   */
  @GetMapping("/healthy")
  public ResponseEntity<String> liveness() {
    // TODO:: Add suitable liveness check
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  @PostMapping(value = "/pay")
  public ResponseEntity<String> pay(@RequestBody Payment payment) {
    Bill bill = this.activePaymentGateway.pay(payment);
    String jsonString = GSON.toJson(bill, Bill.class);
    return new ResponseEntity<>(jsonString, HttpStatus.OK);
  }

  private void initConnectorType() {
    String connectorType = System.getenv(PAYMENT_GW_TYPE_ENV_VAR);
    if (StringUtils.isBlank(connectorType) || !paymentGatewayMap.containsKey(connectorType)) {
      LOGGER.warn(String.format("'%s' environment variable is not set; "
          + "thus defaulting to: %s", PAYMENT_GW_TYPE_ENV_VAR, IN_MEMORY_GATEWAY));
      connectorType = IN_MEMORY_GATEWAY;
    }
    activePaymentGateway = paymentGatewayMap.get(connectorType);
    LOGGER.info(String.format("Active connector type is: %s", connectorType));
  }
}
