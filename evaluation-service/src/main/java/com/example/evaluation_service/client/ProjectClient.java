package com.example.evaluation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "porjectservice", url = "http://localhost:8081/projet")
public interface ProjectClient {

    @GetMapping("/api/projets/getprojet/{id}")
    Map<String, Object> getProjectById(@PathVariable("id") Long id);
}
