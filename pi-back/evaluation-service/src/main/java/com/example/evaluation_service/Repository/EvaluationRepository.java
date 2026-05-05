package com.example.evaluation_service.Repository;

import com.example.evaluation_service.Entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByEvaluatorName(String evaluatorName);

    List<Evaluation> findByUserEmail(String userEmail);

    List<Evaluation> findAll();

    List<Evaluation> findByEvaluatedUserEmail(String evaluatedUserEmail);

    List<Evaluation> findByEvaluatedUserName(String evaluatedUserName);

    List<Evaluation> findByProjectName(String projectName);

    long countByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);
}
