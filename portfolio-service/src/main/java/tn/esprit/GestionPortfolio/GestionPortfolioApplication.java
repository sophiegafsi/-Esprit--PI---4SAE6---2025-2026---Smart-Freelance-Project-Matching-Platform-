package tn.esprit.GestionPortfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GestionPortfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionPortfolioApplication.class, args);
	}
}