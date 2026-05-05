package com.example.recompense.Service;

import com.example.recompense.Entity.Recompense;
import com.example.recompense.Repository.RecompenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecompenseService {

    private final RecompenseRepository recompenseRepository;

    public RecompenseService(RecompenseRepository recompenseRepository) {
        this.recompenseRepository = recompenseRepository;
    }

    public List<Recompense> getAllRecompenses() {
        return recompenseRepository.findAll();
    }

    public Optional<Recompense> getRecompenseById(Long id) {
        return recompenseRepository.findById(id);
    }

    public List<Recompense> getActiveRecompenses() {
        return recompenseRepository.findByIsActiveTrue();
    }

    public Recompense createRecompense(Recompense recompense) {
        return recompenseRepository.save(recompense);
    }

    public Recompense updateRecompense(Long id, Recompense details) {
        Recompense recompense = recompenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recompense non trouvee avec l'ID: " + id));

        recompense.setTitle(details.getTitle());
        recompense.setDescription(details.getDescription());
        recompense.setPointsRequired(details.getPointsRequired());
        recompense.setStock(details.getStock());
        recompense.setImageUrl(details.getImageUrl());
        recompense.setIsActive(details.getIsActive());
        return recompenseRepository.save(recompense);
    }

    public void deleteRecompense(Long id) {
        Recompense recompense = recompenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recompense non trouvee avec l'ID: " + id));
        recompenseRepository.delete(recompense);
    }
}
