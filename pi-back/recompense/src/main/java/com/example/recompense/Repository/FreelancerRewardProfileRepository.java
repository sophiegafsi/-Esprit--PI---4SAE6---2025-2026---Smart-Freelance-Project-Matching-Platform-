package com.example.recompense.Repository;

import com.example.recompense.Entity.FreelancerRewardProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreelancerRewardProfileRepository extends JpaRepository<FreelancerRewardProfile, Long> {

    Optional<FreelancerRewardProfile> findByUserEmail(String userEmail);
}
