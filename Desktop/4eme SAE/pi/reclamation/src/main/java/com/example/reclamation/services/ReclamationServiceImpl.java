package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.clients.ContractClient;
import com.example.reclamation.clients.UserClient;
import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements IReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final ContractClient contractClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public Reclamation createReclamation(Reclamation reclamation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("User must be authenticated with a JWT token.");
        }

        String keycloakId = jwt.getSubject();
        log.info("DEBUG: Creating reclamation for Keycloak ID: {}", keycloakId);
        
        // Resolve internal ID from user microservice
        UUID userInternalId;
        try {
            Map<String, Object> userData = userClient.getUserByKeycloakId(keycloakId);
            if (userData == null || !userData.containsKey("id")) {
                log.error("DEBUG: Internal ID not found for Keycloak ID: {}", keycloakId);
                throw new AccessDeniedException("User account not fully synchronized.");
            }
            userInternalId = UUID.fromString(userData.get("id").toString());
            log.info("DEBUG: Resolved Internal ID: {}", userInternalId);
        } catch (feign.FeignException.NotFound e) {
            log.error("DEBUG: User not found in database for Keycloak ID: {}", keycloakId);
            throw new AccessDeniedException("Your identity is not yet recognized. Please try logging out and back in.");
        } catch (feign.FeignException e) {
            log.error("DEBUG: Feign error calling user service (Status: {}): {}", e.status(), e.getMessage());
            throw new IllegalStateException("Identity resolution failed (Error " + e.status() + "). Please try again later.");
        } catch (Exception e) {
            log.error("DEBUG: Failed to resolve internal ID for Keycloak ID: {}. Error: {}", keycloakId, e.getMessage());
            throw new IllegalStateException("Service temporarily unavailable: identity resolution failed.");
        }
        
        log.info("DEBUG: Authorities found: {}", authentication.getAuthorities());
        
        boolean isFreelancer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_freelancer"));
        boolean isClient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_client"));
        
        log.info("DEBUG: Roles - isFreelancer: {}, isClient: {}", isFreelancer, isClient);

        if (!isFreelancer && !isClient) {
            log.warn("DEBUG: Access denied - user role check failed");
            throw new AccessDeniedException("Only freelancers and clients can create reclamations.");
        }

        // Check for contracts using internal ID
        boolean hasContract = false;
        try {
            if (isFreelancer) {
                List<Object> contracts = contractClient.getContractsByFreelancer(userInternalId);
                log.info("DEBUG: Freelancer contract count: {}", contracts != null ? contracts.size() : 0);
                hasContract = contracts != null && !contracts.isEmpty();
            } else if (isClient) {
                List<Object> contracts = contractClient.getContractsByClient(userInternalId);
                log.info("DEBUG: Client contract count: {}", contracts != null ? contracts.size() : 0);
                hasContract = contracts != null && !contracts.isEmpty();
            }
        } catch (Exception e) {
            log.error("DEBUG: Failed to communicate with condidature microservice: {}", e.getMessage());
            throw new IllegalStateException("Service temporarily unavailable: could not verify contract status.");
        }

        if (!hasContract) {
            log.warn("DEBUG: Access denied - NO CONTRACTS FOUND for user ID: {}", userInternalId);
            throw new AccessDeniedException("You must have at least one active contract to submit a reclamation.");
        }

        reclamation.setIdUtilisateur(userInternalId.toString()); // Store internal ID
        if (reclamation.getStatut() == null) {
            reclamation.setStatut(Statut.EN_ATTENTE);
        }
        return reclamationRepository.save(reclamation);
    }

    @Override
    public List<ReclamationDTO> getAllReclamations() {
        return reclamationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ReclamationDTO getReclamationById(Integer id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + id));

        return mapToDTO(reclamation);
    }

    @Override
    @Transactional
    public Reclamation updateReclamation(Integer id, Reclamation reclamationDetails) {
        Reclamation existing = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + id));

        if (existing.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Cannot modify reclamation because it is already being processed or resolved.");
        }

        existing.setSujet(reclamationDetails.getSujet());
        existing.setDescription(reclamationDetails.getDescription());
        existing.setPriorite(reclamationDetails.getPriorite());
        existing.setType(reclamationDetails.getType());
        existing.setIdCible(reclamationDetails.getIdCible());

        return reclamationRepository.save(existing);
    }

    @Override
    public void deleteReclamation(Integer id) {
        if (!reclamationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reclamation not found with id: " + id);
        }
        reclamationRepository.deleteById(id);
    }

    @Override
    public List<ReclamationDTO> searchReclamations(String search, Type type, Priorite priorite, Statut statut) {
        return reclamationRepository.searchReclamations(search, type, priorite, statut)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private boolean isUrgent(Reclamation reclamation) {
        return reclamation.getPriorite() == Priorite.HAUTE
                || reclamation.getPriorite() == Priorite.CRITIQUE;
    }

    private String getUrgentReason(Reclamation reclamation) {
        if (reclamation.getPriorite() == Priorite.CRITIQUE) {
            return "Priorité critique";
        }

        if (reclamation.getPriorite() == Priorite.HAUTE) {
            return "Priorité élevée";
        }

        return "";
    }

    private ReclamationDTO mapToDTO(Reclamation r) {
        ReclamationDTO dto = new ReclamationDTO();

        dto.setIdReclamation(r.getIdReclamation());
        dto.setSujet(r.getSujet());
        dto.setDescription(r.getDescription());
        dto.setDateCreation(r.getDateCreation());
        dto.setStatut(r.getStatut());
        dto.setPriorite(r.getPriorite());
        dto.setType(r.getType());
        dto.setIdUtilisateur(r.getIdUtilisateur());
        dto.setIdCible(r.getIdCible());

        dto.setUrgent(isUrgent(r));
        dto.setUrgentReason(getUrgentReason(r));

        return dto;
    }
}