package freelink.condidature.repository;

import freelink.condidature.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, UUID> {
    List<Candidature> findByFreelancerId(UUID freelancerId);

    List<Candidature> findByProjectId(UUID projectId);

    Optional<Candidature> findByFreelancerIdAndProjectId(UUID freelancerId, UUID projectId);
}
