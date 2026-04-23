package com.example.eurekaserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EurekaServerApplicationTests {

  @Test
  void applicationClassCanBeInstantiated() {
    assertDoesNotThrow(EurekaServerApplication::new);
  }
}
