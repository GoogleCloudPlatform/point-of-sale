// Copyright 2023 Google LLC
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

package com.google.abmedge.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.logging.log4j.util.Strings;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryIntegrationTest extends AbstractContainerDatabaseTest {

  private static final Logger logger = LoggerFactory.getLogger(InventoryIntegrationTest.class);

  @Value("${local.server.port}")
  private String localServerPort;

  // Create a common docker network so that containers can communicate
  @Rule public static Network network = Network.newNetwork();

  static ToxiproxyClient toxiproxyClient;

  static Proxy apiProxy;

  @Autowired private TestRestTemplate testRestTemplate;

  static String proxyHostAndPort;

  // Toxiproxy container, which will be used as a TCP proxy
  @Rule @Container
  public static ToxiproxyContainer toxiproxy =
      new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0").withNetwork(network);

  @Rule @Container
  public static MySQLContainer<?> mySQLContainer =
      new MySQLContainer<>("mysql:5.7.34")
          .withNetwork(network)
          .withNetworkAliases("mysql")
          .withLogConsumer(new Slf4jLogConsumer(logger))
          .withDatabaseName("pos_db")
          .withUsername("testUser")
          .withPassword("pass");

  @DynamicPropertySource
  static void registerTestProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url",
        () ->
            String.format(
                "jdbc:mysql://%s:%d/pos_db",
                toxiproxy.getHost(), toxiproxy.getFirstMappedPort() + 1));
    registry.add("spring.datasource.username", () -> "testUser");
    registry.add("spring.datasource.password", () -> "pass");
    registry.add("spring.datasource.driverClassName", () -> "com.mysql.jdbc.Driver");
    registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    registry.add("server.tomcat.connection-timeout", () -> "5000");
  }

  @BeforeEach
  void initializeTestProxy() throws IOException {
    if (toxiproxyClient == null) {
      InetAddress hostRunningTheAppEndpoint = InetAddress.getLocalHost();
      toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getFirstMappedPort());
      int toxiproxyPort = 8666;
      apiProxy =
          toxiproxyClient.createProxy(
              "apiProxy",
              "0.0.0.0:" + toxiproxyPort,
              hostRunningTheAppEndpoint.getHostAddress() + ":" + localServerPort);
      proxyHostAndPort =
          String.format(
              "http://%s:%d", toxiproxy.getHost(), toxiproxy.getMappedPort(toxiproxyPort));
    }
  }

  @Test
  void testConnectionCut() throws IOException {
    ResponseEntity<String> itemsBeforeToxic =
        this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);

    assertThat(
            itemsBeforeToxic.getStatusCode().equals(HttpStatus.OK)
                && Strings.isNotBlank(itemsBeforeToxic.getBody()))
        .as("we are able to read and write from the JPARepository before cutting the connection");

    apiProxy.toxics().bandwidth("CUT_CONNECTION_DOWNSTREAM", ToxicDirection.DOWNSTREAM, 0);
    apiProxy.toxics().bandwidth("CUT_CONNECTION_UPSTREAM", ToxicDirection.UPSTREAM, 0);

    assertThat(
            catchThrowable(
                () -> {
                  ResponseEntity<String> itemsDuringToxic =
                      this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);
                }))
        .as("call should fail when the connection is cut")
        .isInstanceOf(ResourceAccessException.class);

    apiProxy.toxics().get("CUT_CONNECTION_DOWNSTREAM").remove();
    apiProxy.toxics().get("CUT_CONNECTION_UPSTREAM").remove();

    ResponseEntity<String> itemsAfterToxic =
        this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);
    assertThat(
            itemsAfterToxic.getStatusCode().equals(HttpStatus.OK)
                && Strings.isNotBlank(itemsAfterToxic.getBody()))
        .as("access is restored once the network returns");
  }

  @Test
  void SecondTest() throws IOException {
    ResponseEntity<String> itemsBeforeToxic =
        this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);

    assertThat(
            itemsBeforeToxic.getStatusCode().equals(HttpStatus.OK)
                && Strings.isNotBlank(itemsBeforeToxic.getBody()))
        .as("we are able to read and write from the JPARepository before cutting the connection");

    apiProxy.toxics().bandwidth("CUT_CONNECTION_DOWNSTREAM", ToxicDirection.DOWNSTREAM, 0);
    apiProxy.toxics().bandwidth("CUT_CONNECTION_UPSTREAM", ToxicDirection.UPSTREAM, 0);

    assertThat(
            catchThrowable(
                () -> {
                  ResponseEntity<String> itemsDuringToxic =
                      this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);
                }))
        .as("call should fail when the connection is cut")
        .isInstanceOf(ResourceAccessException.class);

    apiProxy.toxics().get("CUT_CONNECTION_DOWNSTREAM").remove();
    apiProxy.toxics().get("CUT_CONNECTION_UPSTREAM").remove();

    ResponseEntity<String> itemsAfterToxic =
        this.testRestTemplate.getForEntity(proxyHostAndPort + "/items", String.class);
    assertThat(
            itemsAfterToxic.getStatusCode().equals(HttpStatus.OK)
                && Strings.isNotBlank(itemsAfterToxic.getBody()))
        .as("access is restored once the network returns");
  }
}
