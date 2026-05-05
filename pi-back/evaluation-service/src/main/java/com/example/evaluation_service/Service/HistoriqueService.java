package com.example.evaluation_service.Service;



import com.example.evaluation_service.Entity.Historique;
import com.example.evaluation_service.Repository.HistoriqueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoriqueService {

    private final HistoriqueRepository historiqueRepository;

    public HistoriqueService(HistoriqueRepository historiqueRepository) {
        this.historiqueRepository = historiqueRepository;
    }

    // Créer une entrée d'historique
    public Historique addHistorique(String action, String entite, Long entiteId, String utilisateur, String commentaire) {
        Historique h = new Historique();
        h.setAction(action);
        h.setEntite(entite);
        h.setEntiteId(entiteId);
        h.setUtilisateur(utilisateur);
        h.setDateAction(LocalDateTime.now());
        h.setCommentaire(commentaire);
        return historiqueRepository.save(h);
    }

    // Lire tout
    public List<Historique> getAllHistorique() {
        return historiqueRepository.findAll();
    }

    // Lire par utilisateur
    public List<Historique> getHistoriqueByUser(String utilisateur) {
        return historiqueRepository.findByUtilisateur(utilisateur);
    }

    // Lire par entité
    public List<Historique> getHistoriqueByEntite(String entite) {
        return historiqueRepository.findByEntite(entite);
    }

    // Supprimer une entrée
    public void deleteHistorique(Long id) {
        historiqueRepository.deleteById(id);
    }
}
