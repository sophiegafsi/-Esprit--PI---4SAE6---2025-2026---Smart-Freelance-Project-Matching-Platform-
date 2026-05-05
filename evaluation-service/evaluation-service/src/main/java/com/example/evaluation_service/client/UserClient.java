package com.example.evaluation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user", url = "http://localhost:8081/user")
public interface UserClient {

    @GetMapping("/api/users/email/{email}")
    Map<String, Object> getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") java.util.UUID id);

    @GetMapping("/api/users/keycloak/{keycloakId}")
    Map<String, Object> getUserByKeycloakId(@PathVariable("keycloakId") String keycloakId);
}
