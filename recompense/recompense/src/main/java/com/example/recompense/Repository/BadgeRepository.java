package com.example.recompense.Repository;


import com.example.recompense.Entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByIsActiveTrue();

    List<Badge> findByCategory(String category);

    Badge findByName(String name);

    Optional<Badge> findByNameIgnoreCase(String name);

    List<Badge> findByConditionTypeAndIsActiveTrueOrderByConditionValueAsc(String conditionType);
}
