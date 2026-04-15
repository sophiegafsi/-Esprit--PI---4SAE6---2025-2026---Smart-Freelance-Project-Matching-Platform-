package com.example.reclamation.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "http://localhost:8081/user")
public interface UserClient {

    @GetMapping("/api/users/keycloak/{keycloakId}")
    Map<String, Object> getUserByKeycloakId(@PathVariable("keycloakId") String keycloakId);
}
