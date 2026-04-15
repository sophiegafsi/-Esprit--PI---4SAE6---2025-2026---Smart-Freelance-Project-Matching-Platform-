package com.example.recompense.Repository;

import com.example.recompense.Entity.Recompense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecompenseRepository extends JpaRepository<Recompense, Long> {
    List<Recompense> findByIsActiveTrue();
}
