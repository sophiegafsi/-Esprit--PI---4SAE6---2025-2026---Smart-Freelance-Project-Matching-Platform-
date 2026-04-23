package com.example.apigateway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApiGatewayApplicationTests {

  @Test
  void applicationClassCanBeInstantiated() {
    assertDoesNotThrow(ApiGatewayApplication::new);
  }
}
