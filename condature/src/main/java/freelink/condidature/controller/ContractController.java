package freelink.condidature.controller;

import freelink.condidature.entity.Contract;
import freelink.condidature.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor

public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<Contract> createContract(@RequestBody Contract contract) {
        contract.setStatus(Contract.ContractStatus.PENDING);
        return ResponseEntity.ok(contractService.createContract(contract));
    }

    @PutMapping("/{id}/sign/client")
    public ResponseEntity<Contract> signByClient(@PathVariable("id") UUID id, @RequestBody String signature) {
        return ResponseEntity.ok(contractService.signByClient(id, signature));
    }

    @PutMapping("/{id}/sign/freelancer")
    public ResponseEntity<Contract> signByFreelancer(@PathVariable("id") UUID id, @RequestBody String signature) {
        return ResponseEntity.ok(contractService.signByFreelancer(id, signature));
    }

    @GetMapping
    public ResponseEntity<java.util.List<Contract>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContract(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(contractService.getContract(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<java.util.List<Contract>> getContractsByClient(@PathVariable("clientId") UUID clientId) {
        return ResponseEntity.ok(contractService.getContractsByClient(clientId));
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<java.util.List<Contract>> getContractsByFreelancer(
            @PathVariable("freelancerId") UUID freelancerId) {
        return ResponseEntity.ok(contractService.getContractsByFreelancer(freelancerId));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadContractPdf(@PathVariable("id") UUID id) {
        try {
            byte[] pdfBytes = contractService.generateContractPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contract-" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to generate PDF for contract " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
