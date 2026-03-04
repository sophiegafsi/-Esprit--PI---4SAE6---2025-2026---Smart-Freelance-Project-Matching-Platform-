package freelink.condidature.repository;

import freelink.condidature.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByClientId(UUID clientId);

    List<Contract> findByFreelancerId(UUID freelancerId);

    List<Contract> findByProjectId(UUID projectId);

    List<Contract> findByCandidatureId(UUID candidatureId);
}
