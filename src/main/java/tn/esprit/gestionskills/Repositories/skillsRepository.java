package tn.esprit.gestionskills.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestionskills.Entities.skills;

@Repository
public interface  skillsRepository extends JpaRepository<skills, Long> {

}
