package com.example.reclamation.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "condidature", url = "http://condature:8082")
public interface ContractClient {

    @GetMapping("/api/contracts/client/{clientId}")
    List<Object> getContractsByClient(@PathVariable("clientId") UUID clientId);

    @GetMapping("/api/contracts/freelancer/{freelancerId}")
    List<Object> getContractsByFreelancer(@PathVariable("freelancerId") UUID freelancerId);
}
