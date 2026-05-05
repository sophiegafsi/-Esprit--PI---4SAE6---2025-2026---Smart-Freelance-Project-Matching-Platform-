package com.example.eureka_server;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EurekaServerApplicationTests {

    @MockBean
    private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
	}

}
