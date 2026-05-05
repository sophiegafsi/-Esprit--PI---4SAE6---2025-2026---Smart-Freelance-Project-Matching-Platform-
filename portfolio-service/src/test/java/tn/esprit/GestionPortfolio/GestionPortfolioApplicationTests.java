package tn.esprit.GestionPortfolio;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GestionPortfolioApplicationTests {

    @MockBean
    private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
	}

}
