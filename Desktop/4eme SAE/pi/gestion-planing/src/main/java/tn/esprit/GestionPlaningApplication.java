package tn.esprit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "tn.esprit")
public class GestionPlaningApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestionPlaningApplication.class, args);
    }
}