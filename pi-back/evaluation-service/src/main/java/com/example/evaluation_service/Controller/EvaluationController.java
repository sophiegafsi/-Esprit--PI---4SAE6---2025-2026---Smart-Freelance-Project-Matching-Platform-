package com.example.evaluation_service.Controller;

import com.example.evaluation_service.Entity.Evaluation;
import com.example.evaluation_service.Repository.EvaluationRepository;
import com.example.evaluation_service.Service.EvaluationService;
import org.springframework.security.core.Authentication; // ✅ CORRECT IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationRepository evaluationRepository;

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }


    @PostMapping("/add")
    public Evaluation addEvaluation(@RequestBody Evaluation evaluation) {

        return evaluationService.createEvaluation(evaluation);
    }

    @GetMapping("/all")
    public List<Evaluation> getAllEvaluations() {
        return evaluationService.getAllEvaluations();
    }

    @GetMapping("/user/{email}")
    public List<Evaluation> getEvaluationsByUserEmail(@PathVariable String email) {
        return evaluationService.getEvaluationsByUserEmail(email);
    }

    @GetMapping
    public List<Evaluation> getEvaluations(@RequestParam(required = false) String userEmail) {
        if (userEmail != null && !userEmail.isEmpty()) {
            return evaluationService.getEvaluationsByUserEmail(userEmail);
        }
        return evaluationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evaluation> getEvaluation(@PathVariable Long id) {
        return evaluationService.getEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Evaluation> updateEvaluation(@PathVariable Long id, @RequestBody Evaluation evaluation) {
        return ResponseEntity.ok(evaluationService.updateEvaluation(id, evaluation));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/average/{userName}")
    public ResponseEntity<Double> getAverageScore(@PathVariable String userName) {
        return ResponseEntity.ok(evaluationService.getAverageScoreForUser(userName));
    }
}